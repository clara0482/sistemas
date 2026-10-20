import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("javafx")
class MainFXTest {
    @TempDir Path pasta;
    private Stage janela;
    private final List<String> mensagens = new ArrayList<>();

    @BeforeAll
    static void iniciarJavaFX() throws Exception {
        CountDownLatch iniciou = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            iniciou.countDown();
        });
        assertTrue(iniciou.await(20, TimeUnit.SECONDS), "JavaFX não iniciou");
    }

    @AfterAll
    static void encerrarJavaFX() {
        Platform.exit();
    }

    @SuppressWarnings("unchecked")
    @Test
    void interfaceValidaCadastroReservaAlteraCancelaEConsultaFila() throws Exception {
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
        long clienteId;
        long reservaId;
        LocalDateTime horario = LocalDate.now().plusDays(1).atTime(18, 0);
        try (var fabrica = BancoTeste.abrir(pasta.resolve("interface"))) {
            SistemaReservas sistema = new SistemaReservas(fabrica);
            try {
                naInterface(() -> {
                    janela = new Stage();
                    new MainFX(sistema).start(janela);
                    assertTrue(janela.isShowing());
                    texto("nome", "Teste da interface");
                    texto("telefone", "123");
                    clicarComAlerta("entrar", "10 ou 11");
                    assertNotNull(no("nome"));
                    texto("telefone", "(11) 99999-0101");
                    botao("entrar").fire();
                    assertEquals(3, this.<Restaurante>lista("restaurantes").getItems().size());
                    this.<Restaurante>lista("restaurantes").getSelectionModel().selectFirst();
                    TableView<Mesa> mesas = (TableView<Mesa>) no("mesas");
                    assertEquals(5, mesas.getItems().size());
                    assertEquals(5, mesas.getItems().stream()
                            .filter(m -> "Livre".equals(mesas.getColumns().get(2).getCellData(m))).count());
                    quantidade("quantidade", "9");
                    clicarComAlerta("reservar", "1 a 8");
                    quantidade("quantidade", "8");
                    clicarComAlerta("reservar", "confirmada");
                    assertEquals(1, mesas.getItems().stream()
                            .filter(m -> "Ocupada".equals(mesas.getColumns().get(2).getCellData(m))).count());
                    ComboBox<?> horarios = (ComboBox<?>) no("horario");
                    horarios.getSelectionModel().select(4);
                    assertEquals(5, mesas.getItems().stream()
                            .filter(m -> "Livre".equals(mesas.getColumns().get(2).getCellData(m))).count());
                    horarios.getSelectionModel().selectFirst();
                    selecionarAba(1);
                    assertEquals(1, this.<Reserva>lista("reservas").getItems().size());
                    this.<Reserva>lista("reservas").getSelectionModel().selectFirst();
                    quantidade("quantidade-alteracao", "6");
                    clicarComAlerta("alterar", "Reserva alterada");
                    assertEquals(6, this.<Reserva>lista("reservas").getItems().get(0).getQuantidadePessoas());
                    this.<Reserva>lista("reservas").getSelectionModel().selectFirst();
                    clicarComAlerta("cancelar", "Reserva cancelada");
                    assertEquals(StatusReserva.CANCELADA,
                            this.<Reserva>lista("reservas").getItems().get(0).getStatus());
                    botao("sair").fire();
                    texto("nome", "Nome diferente");
                    texto("telefone", "11999990101");
                    clicarComAlerta("entrar", "outro cadastro");
                    assertNotNull(no("nome"));
                    texto("nome", "Teste da interface");
                    texto("telefone", "11999990101");
                    botao("entrar").fire();
                    selecionarAba(1);
                    assertEquals(1, this.<Reserva>lista("reservas").getItems().size());
                });
                Cliente cliente = sistema.cadastrarCliente("Teste da interface", "11999990101");
                clienteId = cliente.getId();
                assertEquals("Teste da interface", cliente.getNome());
                reservaId = sistema.listarReservas(clienteId).get(0).getId();
                long restaurante = sistema.listarRestaurantes().get(0).getId();
                Cliente ocupante = sistema.cadastrarCliente("Ocupante", "11999990102");
                Reserva ocupada = sistema.solicitarMesa(restaurante, ocupante.getId(), 8, horario);
                naInterface(() -> {
                    selecionarAba(0);
                    this.<Restaurante>lista("restaurantes").getSelectionModel().selectFirst();
                    quantidade("quantidade", "8");
                    clicarComAlerta("reservar", "fila de espera");
                    selecionarAba(2);
                    assertEquals(1, this.<String>lista("fila").getItems().size());
                    assertTrue(this.<String>lista("fila").getItems().get(0).contains("posição 1"));
                });
                assertTrue(sistema.cancelarReserva(ocupada.getId(), ocupante.getId()));
                naInterface(() -> {
                    selecionarAba(1);
                    assertEquals(2, this.<Reserva>lista("reservas").getItems().size());
                    selecionarAba(2);
                    assertTrue(lista("fila").getItems().isEmpty());
                });
            } finally {
                naInterface(() -> {
                    for (Window window : List.copyOf(Window.getWindows())) {
                        window.hide();
                    }
                });
            }
        }
        try (var reaberta = BancoTeste.abrir(pasta.resolve("interface"))) {
            SistemaReservas recuperado = new SistemaReservas(reaberta);
            assertEquals(2, recuperado.listarReservas(clienteId).size());
            assertEquals(StatusReserva.CANCELADA, recuperado.listarReservas(clienteId).stream()
                    .filter(r -> r.getId().equals(reservaId)).findFirst().orElseThrow().getStatus());
            assertTrue(recuperado.listarReservas(clienteId).stream()
                    .anyMatch(r -> r.getStatus() == StatusReserva.ATIVA && r.getQuantidadePessoas() == 8));
        }
    }

    private void selecionarAba(int indice) {
        ((TabPane) no("abas")).getSelectionModel().select(indice);
        janela.getScene().getRoot().applyCss();
        janela.getScene().getRoot().layout();
    }

    private Node no(String id) {
        Node node = janela.getScene().lookup("#" + id);
        assertNotNull(node, "Componente não encontrado: " + id);
        return node;
    }

    @SuppressWarnings("unchecked")
    private <T> ListView<T> lista(String id) {
        return (ListView<T>) no(id);
    }

    private Button botao(String id) {
        return (Button) no(id);
    }

    private void texto(String id, String valor) {
        ((TextField) no(id)).setText(valor);
    }

    private void quantidade(String id, String valor) {
        ((Spinner<?>) no(id)).getEditor().setText(valor);
    }

    private void clicarComAlerta(String id, String esperado) {
        mensagens.clear();
        Timeline fecharDialogos = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window != janela && window.isShowing()
                        && window.getScene().getRoot() instanceof DialogPane dialogo) {
                    mensagens.add(dialogo.getContentText());
                    window.hide();
                }
            }
        }));
        fecharDialogos.setCycleCount(Timeline.INDEFINITE);
        fecharDialogos.play();
        try {
            botao(id).fire();
            assertTrue(mensagens.stream().anyMatch(m -> m.contains(esperado)),
                    "Alerta esperado: " + esperado + "; recebido: " + mensagens);
        } finally {
            fecharDialogos.stop();
        }
    }

    private void naInterface(Runnable acao) throws Exception {
        FutureTask<Void> tarefa = new FutureTask<>(acao, null);
        Platform.runLater(tarefa);
        tarefa.get(45, TimeUnit.SECONDS);
    }
}