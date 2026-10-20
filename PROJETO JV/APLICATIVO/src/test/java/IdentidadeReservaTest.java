import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.hibernate.SessionFactory;

class IdentidadeReservaTest {
    @TempDir Path pasta;
    private final LocalDateTime horario = LocalDate.now().plusDays(2).atTime(19, 0);

    @Test
    void telefoneExistenteNaoPermiteAssumirOutroCliente() {
        try (var fabrica = BancoTeste.abrir(pasta.resolve("identidade"))) {
            var sistema = new SistemaReservas(fabrica);
            sistema.cadastrarRestaurantesIniciais();
            long restaurante = sistema.listarRestaurantes().get(0).getId();
            var ana = sistema.cadastrarCliente("Ana", "(11) 99999-0001");
            var reserva = sistema.solicitarMesa(restaurante, ana.getId(), 2, horario);
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCliente("Bia", "11999990001"));
            assertEquals(ana.getId(), sistema.cadastrarCliente(" ana ", "11999990001").getId());
            try (var session = fabrica.openSession()) {
                assertEquals(1L, session.createQuery("select count(c) from Cliente c", Long.class).getSingleResult());
            }
            assertEquals(StatusReserva.ATIVA, sistema.listarReservas(ana.getId()).get(0).getStatus());
            assertEquals(reserva.getId(), sistema.listarReservas(ana.getId()).get(0).getId());
        }
    }

    @Test
    void clientesDistintosTemIdsReservasECancelamentosIndependentes() {
        long anaId, biaId, primeiraId, segundaId;
        try (var fabrica = BancoTeste.abrir(pasta.resolve("independentes"))) {
            var sistema = new SistemaReservas(fabrica);
            sistema.cadastrarRestaurantesIniciais();
            long restaurante = sistema.listarRestaurantes().get(0).getId();
            var ana = sistema.cadastrarCliente("Ana", "11999990001");
            var bia = sistema.cadastrarCliente("Bia", "11999990002");
            anaId = ana.getId();
            biaId = bia.getId();
            var primeira = sistema.solicitarMesa(restaurante, anaId, 2, horario);
            var segunda = sistema.solicitarMesa(restaurante, biaId, 2, horario);
            primeiraId = primeira.getId();
            segundaId = segunda.getId();
            assertNotEquals(anaId, biaId);
            assertNotEquals(primeiraId, segundaId);
            assertNotEquals(primeira.getMesa().getId(), segunda.getMesa().getId());
            assertFalse(sistema.cancelarReserva(primeiraId, biaId));
            assertTrue(sistema.cancelarReserva(primeiraId, anaId));
            assertFalse(sistema.cancelarReserva(primeiraId, anaId));
            assertEquals(List.of(segundaId), sistema.listarReservas(biaId).stream().map(Reserva::getId).toList());
            assertEquals(StatusReserva.ATIVA, sistema.listarReservas(biaId).get(0).getStatus());
            verificarSemConflitos(fabrica);
        }
        try (var fabrica = BancoTeste.abrir(pasta.resolve("independentes"))) {
            var sistema = new SistemaReservas(fabrica);
            assertEquals(primeiraId, sistema.listarReservas(anaId).get(0).getId());
            assertEquals(StatusReserva.CANCELADA, sistema.listarReservas(anaId).get(0).getStatus());
            assertEquals(segundaId, sistema.listarReservas(biaId).get(0).getId());
            assertEquals(StatusReserva.ATIVA, sistema.listarReservas(biaId).get(0).getStatus());
        }
    }

    @Test
    void mesmaMesaEmHorariosConsecutivosNaoCompartilhaCancelamento() {
        try (var fabrica = BancoTeste.abrir(pasta.resolve("consecutivas"))) {
            var sistema = new SistemaReservas(fabrica);
            sistema.cadastrarRestaurantesIniciais();
            long restaurante = sistema.listarRestaurantes().get(0).getId();
            var ana = sistema.cadastrarCliente("Ana", "11999990001");
            var bia = sistema.cadastrarCliente("Bia", "11999990002");
            var primeira = sistema.solicitarMesa(restaurante, ana.getId(), 8, horario);
            var segunda = sistema.solicitarMesa(restaurante, bia.getId(), 8, horario.plusHours(2));
            assertEquals(primeira.getMesa().getId(), segunda.getMesa().getId());
            assertNotEquals(primeira.getId(), segunda.getId());
            assertTrue(sistema.cancelarReserva(primeira.getId(), ana.getId()));
            assertEquals(StatusReserva.ATIVA, sistema.listarReservas(bia.getId()).get(0).getStatus());
            verificarSemConflitos(fabrica);
        }
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {0, 30})
    void concorrenciaEntreServicosNaoDuplicaMesaComSobreposicao(int minutos) throws Exception {
        try (var fabrica = BancoTeste.abrir(pasta.resolve("concorrencia"))) {
            var sistema = new SistemaReservas(fabrica);
            sistema.cadastrarRestaurantesIniciais();
            long restaurante = sistema.listarRestaurantes().get(0).getId();
            long ana = sistema.cadastrarCliente("Ana", "11999990001").getId();
            long bia = sistema.cadastrarCliente("Bia", "11999990002").getId();
            var executor = Executors.newFixedThreadPool(2);
            var prontas = new CountDownLatch(2);
            var iniciar = new CountDownLatch(1);
            try {
                var a = executor.submit(() -> {
                    prontas.countDown();
                    assertTrue(iniciar.await(10, TimeUnit.SECONDS));
                    return new SistemaReservas(fabrica).solicitarMesa(restaurante, ana, 8, horario);
                });
                var b = executor.submit(() -> {
                    prontas.countDown();
                    assertTrue(iniciar.await(10, TimeUnit.SECONDS));
                    return new SistemaReservas(fabrica).solicitarMesa(restaurante, bia, 8, horario.plusMinutes(minutos));
                });
                assertTrue(prontas.await(10, TimeUnit.SECONDS));
                iniciar.countDown();
                var primeira = a.get(20, TimeUnit.SECONDS);
                var segunda = b.get(20, TimeUnit.SECONDS);
                assertTrue((primeira == null) != (segunda == null));
                assertEquals(1, sistema.consultarFila(primeira == null ? ana : bia).size());
                verificarSemConflitos(fabrica);
            } finally {
                iniciar.countDown();
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    private void verificarSemConflitos(SessionFactory fabrica) {
        try (var session = fabrica.openSession()) {
            var linhas = session.createNativeQuery(
                    "select id, cliente_id, mesa_id, horario, status from reserva order by id", Object[].class).list();
            linhas.forEach(r -> System.out.println("PERSISTIDO: " + java.util.Arrays.toString(r)));
            var ativas = session.createQuery("from Reserva where status = :status", Reserva.class)
                    .setParameter("status", StatusReserva.ATIVA).list();
            for (int i = 0; i < ativas.size(); i++) {
                for (int j = i + 1; j < ativas.size(); j++) {
                    var a = ativas.get(i);
                    var b = ativas.get(j);
                    assertFalse(a.getMesa().getId().equals(b.getMesa().getId())
                            && Reserva.horariosSobrepostos(a.getHorario(), b.getHorario()));
                }
            }
        }
    }
}