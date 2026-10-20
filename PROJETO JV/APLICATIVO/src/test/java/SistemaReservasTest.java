import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SistemaReservasTest {
    @TempDir Path pasta;
    private SessionFactory fabrica;
    private SistemaReservas sistema;
    private long restaurante;
    private final LocalDateTime horario = LocalDate.now().plusDays(2).atTime(19, 0);

    @BeforeEach
    void preparar() {
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
        fabrica = BancoTeste.abrir(pasta.resolve("reservas"));
        sistema = new SistemaReservas(fabrica);
        sistema.cadastrarRestaurantesIniciais();
        restaurante = sistema.listarRestaurantes().get(0).getId();
    }

    @AfterEach
    void fechar() {
        if (fabrica != null && !fabrica.isClosed()) {
            fabrica.close();
        }
    }

    @Test
    void cadastroValidaNormalizaEReutilizaClienteSemSobrescrever() {
        Cliente cliente = sistema.cadastrarCliente("Ana", "(11) 99999-0001");
        Cliente repetido = sistema.cadastrarCliente("Ana", "11999990001");
        assertEquals(cliente.getId(), repetido.getId());
        assertEquals("Ana", repetido.getNome());
        assertEquals("11999990001", repetido.getTelefone());
        assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarCliente(" ", "11999990001"));
        assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarCliente("Ana", "123"));
    }

    @Test
    void inicializacaoPreservaRestaurantesEMesasExistentes() {
        sistema.cadastrarRestaurantesIniciais();
        assertEquals(3, sistema.listarRestaurantes().size());
        for (Restaurante r : sistema.listarRestaurantes()) {
            assertEquals(5, sistema.listarMesas(r.getId(), horario).size());
        }
    }

    @Test
    void disponibilidadeConsideraSobreposicaoEPermiteReservasConsecutivas() {
        Cliente cliente = sistema.cadastrarCliente("Ana", "11999990001");
        Reserva reserva = sistema.solicitarMesa(restaurante, cliente.getId(), 2, horario);
        assertNotNull(reserva);
        assertFalse(livre(reserva.getMesa().getId(), horario));
        assertFalse(livre(reserva.getMesa().getId(), horario.plusMinutes(30)));
        assertTrue(livre(reserva.getMesa().getId(), horario.plusHours(2)));
        assertEquals(reserva.getId(), sistema.solicitarMesa(restaurante, cliente.getId(), 2, horario).getId());
        long outro = sistema.listarRestaurantes().get(1).getId();
        assertThrows(IllegalArgumentException.class,
                () -> sistema.solicitarMesa(outro, cliente.getId(), 2, horario.plusMinutes(30)));
        assertNotNull(sistema.solicitarMesa(outro, cliente.getId(), 2, horario.plusHours(2)));
    }

    @Test
    void semMesaEntraNaFilaEOCancelamentoAtendeOPedidoOriginal() {
        Cliente primeiro = sistema.cadastrarCliente("Ana", "11999990001");
        Cliente segundo = sistema.cadastrarCliente("Bia", "11999990002");
        Reserva ocupada = sistema.solicitarMesa(restaurante, primeiro.getId(), 8, horario);
        assertNull(sistema.solicitarMesa(restaurante, segundo.getId(), 8, horario));
        assertNull(sistema.solicitarMesa(restaurante, segundo.getId(), 8, horario));
        assertEquals(1, sistema.consultarFila(segundo.getId()).size());
        assertEquals(1, sistema.consultarFila(segundo.getId()).values().iterator().next());
        assertThrows(IllegalArgumentException.class,
                () -> sistema.solicitarMesa(restaurante, segundo.getId(), 6, horario));
        assertTrue(sistema.cancelarReserva(ocupada.getId(), primeiro.getId()));
        assertTrue(sistema.consultarFila(segundo.getId()).isEmpty());
        Reserva atendida = sistema.listarReservas(segundo.getId()).get(0);
        assertEquals(horario, atendida.getHorario());
        assertEquals(8, atendida.getQuantidadePessoas());
        assertEquals(StatusReserva.ATIVA, atendida.getStatus());
    }

    @Test
    void alterarPreservaIdEHorarioELiberaMesaAnterior() {
        Cliente cliente = sistema.cadastrarCliente("Ana", "11999990001");
        Reserva original = sistema.solicitarMesa(restaurante, cliente.getId(), 2, horario);
        assertTrue(sistema.alterarReserva(original.getId(), cliente.getId(), 6));
        Reserva alterada = sistema.listarReservas(cliente.getId()).get(0);
        assertEquals(original.getId(), alterada.getId());
        assertEquals(horario, alterada.getHorario());
        assertEquals(6, alterada.getQuantidadePessoas());
        assertNotEquals(original.getMesa().getId(), alterada.getMesa().getId());
        assertTrue(livre(original.getMesa().getId(), horario));
        assertFalse(livre(alterada.getMesa().getId(), horario));
    }

    @Test
    void trocaSemMesaMantemReservaOriginalERejeitaOutroDono() {
        Cliente primeiro = sistema.cadastrarCliente("Ana", "11999990001");
        Cliente segundo = sistema.cadastrarCliente("Bia", "11999990002");
        sistema.solicitarMesa(restaurante, primeiro.getId(), 8, horario);
        Reserva original = sistema.solicitarMesa(restaurante, segundo.getId(), 2, horario);
        assertFalse(sistema.alterarReserva(original.getId(), segundo.getId(), 8));
        Reserva mantida = sistema.listarReservas(segundo.getId()).get(0);
        assertEquals(original.getMesa().getId(), mantida.getMesa().getId());
        assertEquals(2, mantida.getQuantidadePessoas());
        assertFalse(sistema.cancelarReserva(original.getId(), primeiro.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> sistema.alterarReserva(original.getId(), primeiro.getId(), 4));
    }

    @Test
    void validacoesRecusamPessoasEDatasInvalidas() {
        Cliente cliente = sistema.cadastrarCliente("Ana", "11999990001");
        assertThrows(IllegalArgumentException.class,
                () -> sistema.solicitarMesa(restaurante, cliente.getId(), 9, horario));
        assertThrows(IllegalArgumentException.class,
                () -> sistema.solicitarMesa(restaurante, cliente.getId(), 0, horario));
        assertThrows(IllegalArgumentException.class,
                () -> sistema.solicitarMesa(restaurante, cliente.getId(), 2, horario.minusDays(3)));
        assertThrows(IllegalArgumentException.class,
                () -> sistema.solicitarMesa(restaurante, cliente.getId(), 2, horario.withHour(12)));
        assertThrows(DateTimeParseException.class, () -> Main.interpretarHorario("31/02/2030", "19:00"));
        assertTrue(sistema.listarReservas(cliente.getId()).isEmpty());
    }

    @Test
    void reabrirH2MantemCadastroReservasEFila() {
        Cliente primeiro = sistema.cadastrarCliente("Ana", "11999990001");
        Cliente segundo = sistema.cadastrarCliente("Bia", "11999990002");
        Reserva original = sistema.solicitarMesa(restaurante, primeiro.getId(), 8, horario);
        assertNull(sistema.solicitarMesa(restaurante, segundo.getId(), 8, horario));
        fabrica.close();
        fabrica = BancoTeste.abrir(pasta.resolve("reservas"));
        sistema = new SistemaReservas(fabrica);
        sistema.cadastrarRestaurantesIniciais();
        assertEquals(primeiro.getId(), sistema.cadastrarCliente("Ana", "11999990001").getId());
        Reserva recuperada = sistema.listarReservas(primeiro.getId()).get(0);
        assertEquals(original.getId(), recuperada.getId());
        assertEquals(8, recuperada.getQuantidadePessoas());
        assertFalse(livre(recuperada.getMesa().getId(), horario));
        assertEquals(1, sistema.consultarFila(segundo.getId()).size());
    }

    @Test
    void reservaAntigaSemQuantidadePermaneceConsultavelEAlteravel() {
        Cliente cliente = sistema.cadastrarCliente("Ana", "11999990001");
        Reserva reserva = sistema.solicitarMesa(restaurante, cliente.getId(), 2, horario);
        try (var session = fabrica.openSession()) {
            var tx = session.beginTransaction();
            session.createMutationQuery("update Reserva set quantidadePessoas = null where id = :id")
                    .setParameter("id", reserva.getId()).executeUpdate();
            tx.commit();
        }
        assertNull(sistema.listarReservas(cliente.getId()).get(0).getQuantidadePessoas());
        assertTrue(sistema.alterarReserva(reserva.getId(), cliente.getId(), 2));
        assertEquals(2, sistema.listarReservas(cliente.getId()).get(0).getQuantidadePessoas());
    }

    private boolean livre(long mesaId, LocalDateTime inicio) {
        return sistema.listarMesas(restaurante, inicio).entrySet().stream()
                .filter(item -> item.getKey().getId().equals(mesaId))
                .map(Map.Entry::getValue).findFirst().orElseThrow();
    }
}