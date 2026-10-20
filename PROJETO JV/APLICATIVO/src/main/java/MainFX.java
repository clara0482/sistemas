import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class MainFX extends Application {

    private static final String CSS = """
            .root { -fx-font-family: 'Segoe UI', 'Arial'; -fx-font-size: 14px; -fx-background-color: #f4f5f7; }
            .titulo { -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222; }
            .subtitulo { -fx-font-size: 13px; -fx-text-fill: #666; }
            .secao { -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333; }
            .card { -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 2); }
            .header { -fx-background-color: #c0392b; -fx-padding: 14 20 14 20; }
            .header .label { -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; }
            .botao { -fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;
                     -fx-background-radius: 6; -fx-padding: 8 18 8 18; -fx-cursor: hand; }
            .botao:hover { -fx-background-color: #a93226; }
            .botao:disabled { -fx-background-color: #bbb; }
            .botao-sair { -fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white;
                          -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; }
            .botao-sair:hover { -fx-background-color: rgba(255,255,255,0.2); }
            .horario-vazio { -fx-text-fill: #888; }
            """;

    private final ArrayList<Restaurante> restaurantes = new ArrayList<>();
    private SistemaReservas sistema;
    private final Map<Long, Boolean> disponibilidade = new HashMap<>();
    private DatePicker dataReserva;
    private ListView<Reserva> listaReservas;
    private Cliente clienteAtivo;
    private Stage stage;

    public MainFX() {
    }

    MainFX(SistemaReservas sistema) {
        this.sistema = sistema;
    }

    private Restaurante restauranteSelecionado;
    private Label lblNomeRestaurante;
    private ComboBox<String> cbHorarios;
    private TableView<Mesa> tabelaMesas;
    private Spinner<Integer> spQuantidade;
    private Button btnReservar;
    private ListView<String> listaFila;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        if (sistema == null) {
            sistema = new SistemaReservas();
        }
        sistema.cadastrarRestaurantesIniciais();
        restaurantes.addAll(sistema.listarRestaurantes());

        stage.setTitle("App de Reservas");
        mostrarTelaCadastro();
        stage.show();
    }

    private void mostrarTelaCadastro() {
        Label titulo = new Label("Bem-vindo ao App de Reservas");
        titulo.getStyleClass().add("titulo");
        Label sub = new Label("Realize seu cadastro para continuar");
        sub.getStyleClass().add("subtitulo");

        TextField txtNome = new TextField();
        txtNome.setId("nome");
        txtNome.setPromptText("Seu nome");
        TextField txtTelefone = new TextField();
        txtTelefone.setId("telefone");
        txtTelefone.setPromptText("Seu telefone");

        Button btnEntrar = new Button("Entrar");
        btnEntrar.setId("entrar");
        btnEntrar.getStyleClass().add("botao");
        btnEntrar.setMaxWidth(Double.MAX_VALUE);
        btnEntrar.setDefaultButton(true);

        btnEntrar.setOnAction(e -> {
            String nome = txtNome.getText().trim();
            String telefone = txtTelefone.getText().trim();
            if (nome.isEmpty() || telefone.isEmpty()) {
                alerta(Alert.AlertType.WARNING, "Cadastro", "Preencha o nome e o telefone.");
                return;
            }
            executar(() -> {
                clienteAtivo = sistema.cadastrarCliente(nome, telefone);
                mostrarTelaPrincipal();
            });
        });

        VBox card = new VBox(12, titulo, sub, new Label("Nome"), txtNome, new Label("Telefone"), txtTelefone, btnEntrar);
        card.getStyleClass().add("card");
        card.setMaxWidth(380);

        StackCentro raiz = new StackCentro(card);
        trocarCena(raiz, 700, 500);
    }

    private void mostrarTelaPrincipal() {
        Label ola = new Label("Olá, " + clienteAtivo.getNome() + "!");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        Button btnSair = new Button("Sair");
        btnSair.setId("sair");
        btnSair.getStyleClass().add("botao-sair");
        btnSair.setOnAction(e -> {
            clienteAtivo = null;
            restauranteSelecionado = null;
            mostrarTelaCadastro();
        });
        HBox header = new HBox(10, ola, espaco, btnSair);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        Tab abaReserva = new Tab("Restaurantes e Reserva", construirAbaReserva());
        abaReserva.setClosable(false);
        Tab abaFila = new Tab("Minha posição na fila", construirAbaFila());
        abaFila.setClosable(false);

        Tab abaMinhasReservas = new Tab("Minhas reservas", construirAbaMinhasReservas());
        abaMinhasReservas.setClosable(false);
        TabPane abas = new TabPane(abaReserva, abaMinhasReservas, abaFila);
        abas.setId("abas");
        abas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) -> {
            if (nova == abaFila) {
                executar(this::atualizarFila);
            } else if (nova == abaMinhasReservas) {
                executar(this::atualizarReservas);
            } else if (nova == abaReserva) {
                executar(this::atualizarMesas);
            }
        });

        BorderPane raiz = new BorderPane();
        raiz.setTop(header);
        raiz.setCenter(abas);
        trocarCena(raiz, 980, 620);
    }

    private Region construirAbaReserva() {
        Label lblRest = new Label("Restaurantes");
        lblRest.getStyleClass().add("secao");
        ListView<Restaurante> listaRestaurantes = new ListView<>(FXCollections.observableArrayList(restaurantes));
        listaRestaurantes.setId("restaurantes");
        listaRestaurantes.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Restaurante r, boolean vazio) {
                super.updateItem(r, vazio);
                setText(vazio || r == null ? null : r.getNome());
            }
        });
        VBox.setVgrow(listaRestaurantes, Priority.ALWAYS);
        VBox esquerda = new VBox(10, lblRest, listaRestaurantes);
        esquerda.getStyleClass().add("card");
        esquerda.setPrefWidth(300);

        lblNomeRestaurante = new Label("Selecione um restaurante");
        lblNomeRestaurante.getStyleClass().add("titulo");

        dataReserva = new DatePicker(LocalDate.now().plusDays(1));
        dataReserva.setId("data-reserva");
        dataReserva.setEditable(false);
        dataReserva.setOnAction(e -> executar(this::atualizarMesas));
        cbHorarios = new ComboBox<>();
        cbHorarios.setId("horario");
        cbHorarios.setItems(FXCollections.observableArrayList(
                SistemaReservas.HORARIOS.stream().map(h -> h.toString()).toList()));
        cbHorarios.setOnAction(e -> executar(this::atualizarMesas));
        cbHorarios.setPromptText("Escolha o horário");
        cbHorarios.setPrefWidth(180);

        tabelaMesas = new TableView<>();
        tabelaMesas.setId("mesas");
        tabelaMesas.setPlaceholder(new Label("Escolha um restaurante para ver o mapa de mesas"));
        tabelaMesas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Mesa, Number> colNumero = new TableColumn<>("Mesa");
        colNumero.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumero()));

        TableColumn<Mesa, Number> colCapacidade = new TableColumn<>("Capacidade (pessoas)");
        colCapacidade.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCapacidade()));

        TableColumn<Mesa, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(Boolean.TRUE.equals(disponibilidade.get(c.getValue().getId())) ? "Livre" : "Ocupada"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean vazio) {
                super.updateItem(status, vazio);
                if (vazio || status == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                } else {
                    setText(status);
                    setTextFill(status.equals("Livre") ? Color.web("#1e8449") : Color.web("#c0392b"));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        tabelaMesas.getColumns().add(colNumero);
        tabelaMesas.getColumns().add(colCapacidade);
        tabelaMesas.getColumns().add(colStatus);
        VBox.setVgrow(tabelaMesas, Priority.ALWAYS);

        spQuantidade = new Spinner<>(1, 8, 2);
        spQuantidade.setId("quantidade");
        spQuantidade.setEditable(true);
        spQuantidade.setPrefWidth(90);

        btnReservar = new Button("Reservar mesa");
        btnReservar.setId("reservar");
        btnReservar.getStyleClass().add("botao");
        btnReservar.setDisable(true);
        btnReservar.setOnAction(e -> executar(this::reservar));

        HBox linhaReserva = new HBox(12,
                new Label("Pessoas:"), spQuantidade,
                new Label("Horário:"), cbHorarios,
                btnReservar);
        linhaReserva.setAlignment(Pos.CENTER_LEFT);

        Label lblMesas = new Label("Mapa de mesas do local");
        lblMesas.getStyleClass().add("secao");

        HBox linhaData = new HBox(12, new Label("Data:"), dataReserva);
        linhaData.setAlignment(Pos.CENTER_LEFT);
        VBox direita = new VBox(12, lblNomeRestaurante, linhaData, lblMesas, tabelaMesas, linhaReserva);
        direita.getStyleClass().add("card");
        HBox.setHgrow(direita, Priority.ALWAYS);

        listaRestaurantes.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                executar(() -> selecionarRestaurante(novo));
            }
        });

        HBox conteudo = new HBox(16, esquerda, direita);
        conteudo.setPadding(new Insets(16));
        return conteudo;
    }

    private void selecionarRestaurante(Restaurante r) {
        restauranteSelecionado = r;
        lblNomeRestaurante.setText(r.getNome());
        if (cbHorarios.getValue() == null) {
            cbHorarios.getSelectionModel().selectFirst();
        }
        atualizarMesas();
    }

    private LocalDateTime horarioSelecionado() {
        if (dataReserva.getValue() == null || cbHorarios.getValue() == null) {
            throw new IllegalArgumentException("Escolha a data e o horário da reserva.");
        }
        return Main.interpretarHorario(
                dataReserva.getValue().format(DateTimeFormatter.ofPattern("dd/MM/uuuu")),
                cbHorarios.getValue());
    }

    private void atualizarMesas() {
        tabelaMesas.getItems().clear();
        disponibilidade.clear();
        btnReservar.setDisable(true);
        if (restauranteSelecionado == null) {
            return;
        }
        Map<Mesa, Boolean> mesas = sistema.listarMesas(restauranteSelecionado.getId(), horarioSelecionado());
        mesas.forEach((mesa, livre) -> disponibilidade.put(mesa.getId(), livre));
        tabelaMesas.setItems(FXCollections.observableArrayList(mesas.keySet()));
        btnReservar.setDisable(false);
    }

    private void reservar() {
        if (restauranteSelecionado == null) {
            return;
        }
        int quantidade;
        try {
            quantidade = Integer.parseInt(spQuantidade.getEditor().getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Informe uma quantidade de pessoas válida.");
        }
        Reserva reserva = sistema.solicitarMesa(restauranteSelecionado.getId(), clienteAtivo.getId(),
                quantidade, horarioSelecionado());
        atualizarMesas();
        if (reserva != null) {
            alerta(Alert.AlertType.INFORMATION, "Reserva confirmada",
                    "Reserva #" + reserva.getId() + " confirmada na mesa " + reserva.getMesa().getNumero()
                            + " para " + reserva.getQuantidadePessoas() + " pessoas.");
        } else {
            alerta(Alert.AlertType.INFORMATION, "Fila de espera",
                    "Sem mesa disponível. Sua solicitação está na fila de espera.");
        }
    }

    private Region construirAbaMinhasReservas() {
        listaReservas = new ListView<>();
        listaReservas.setId("reservas");
        listaReservas.setPlaceholder(new Label("Você ainda não tem reservas."));
        listaReservas.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reserva reserva, boolean vazio) {
                super.updateItem(reserva, vazio);
                if (vazio || reserva == null) {
                    setText(null);
                    return;
                }
                String pessoas = reserva.getQuantidadePessoas() == null
                        ? "Quantidade não informada (reserva antiga)"
                        : reserva.getQuantidadePessoas() + " pessoas";
                setText("#" + reserva.getId() + " | " + reserva.getMesa().getRestaurante().getNome()
                        + " | Mesa " + reserva.getMesa().getNumero() + " | "
                        + reserva.getHorario().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        + " | " + pessoas + " | " + reserva.getStatus());
            }
        });
        VBox.setVgrow(listaReservas, Priority.ALWAYS);
        Spinner<Integer> quantidade = new Spinner<>(1, 8, 2);
        quantidade.setId("quantidade-alteracao");
        quantidade.setEditable(true);
        quantidade.setPrefWidth(90);
        Button alterar = new Button("Alterar reserva");
        alterar.setId("alterar");
        alterar.setOnAction(e -> executar(() -> {
            Reserva reserva = reservaSelecionada();
            int pessoas;
            try {
                pessoas = Integer.parseInt(quantidade.getEditor().getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Informe uma quantidade de pessoas válida.");
            }
            boolean alterada = sistema.alterarReserva(reserva.getId(), clienteAtivo.getId(), pessoas);
            atualizarReservas();
            alerta(Alert.AlertType.INFORMATION, "Alterar reserva", alterada
                    ? "Reserva alterada."
                    : "Sem mesa disponível para a troca, considerando a prioridade da fila. A reserva original foi mantida.");
        }));
        Button cancelar = new Button("Cancelar reserva");
        cancelar.setId("cancelar");
        cancelar.setOnAction(e -> executar(() -> {
            Reserva reserva = reservaSelecionada();
            boolean cancelada = sistema.cancelarReserva(reserva.getId(), clienteAtivo.getId());
            atualizarReservas();
            alerta(Alert.AlertType.INFORMATION, "Cancelar reserva", cancelada
                    ? "Reserva cancelada." : "Reserva não encontrada ou já encerrada.");
        }));
        Button atualizar = new Button("Atualizar");
        atualizar.setOnAction(e -> executar(this::atualizarReservas));
        HBox botoes = new HBox(12, new Label("Pessoas:"), quantidade, alterar, cancelar, atualizar);
        botoes.setAlignment(Pos.CENTER_LEFT);
        VBox conteudo = new VBox(12, listaReservas, botoes);
        conteudo.setPadding(new Insets(16));
        return conteudo;
    }

    private Reserva reservaSelecionada() {
        Reserva reserva = listaReservas.getSelectionModel().getSelectedItem();
        if (reserva == null) {
            throw new IllegalArgumentException("Selecione uma reserva.");
        }
        return reserva;
    }

    private void atualizarEstado() {
        atualizarReservas();
        atualizarMesas();
        atualizarFila();
    }

    private void atualizarReservas() {
        listaReservas.setItems(FXCollections.observableArrayList(sistema.listarReservas(clienteAtivo.getId())));
    }

    private Region construirAbaFila() {
        Label titulo = new Label("Status da fila");
        titulo.getStyleClass().add("secao");

        listaFila = new ListView<>();
        listaFila.setId("fila");
        Label vazio = new Label("Você não está aguardando na fila de nenhum restaurante no momento.");
        vazio.getStyleClass().add("horario-vazio");
        listaFila.setPlaceholder(vazio);
        VBox.setVgrow(listaFila, Priority.ALWAYS);

        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.getStyleClass().add("botao");
        btnAtualizar.setOnAction(e -> executar(this::atualizarFila));

        VBox card = new VBox(12, titulo, listaFila, btnAtualizar);
        card.getStyleClass().add("card");

        VBox conteudo = new VBox(card);
        conteudo.setPadding(new Insets(16));
        VBox.setVgrow(card, Priority.ALWAYS);
        return conteudo;
    }

    private void atualizarFila() {
        listaFila.getItems().clear();
        for (Map.Entry<RegistroFila, Integer> item : sistema.consultarFila(clienteAtivo.getId()).entrySet()) {
            RegistroFila registro = item.getKey();
            listaFila.getItems().add(registro.getRestaurante().getNome() + " | "
                    + registro.horarioDesejado().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    + ": posição " + item.getValue()
                    + ". Atendimento conforme horário, capacidade e disponibilidade.");
        }
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException e) {
            alerta(Alert.AlertType.WARNING, "Não foi possível concluir a operação", e.getMessage());
        }
    }

    @Override
    public void stop() {
        HibernateUtil.fechar();
    }

    private void trocarCena(Region raiz, double largura, double altura) {
        Scene cena = new Scene(raiz, largura, altura);
        String css = "data:text/css;base64,"
                + Base64.getEncoder().encodeToString(CSS.getBytes(StandardCharsets.UTF_8));
        cena.getStylesheets().add(css);
        stage.setScene(cena);
        stage.centerOnScreen();
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensagem);
        a.initOwner(stage);
        a.showAndWait();
    }


    private static class StackCentro extends javafx.scene.layout.StackPane {
        StackCentro(javafx.scene.Node filho) {
            getChildren().add(filho);
            setAlignment(Pos.CENTER);
            setPadding(new Insets(30));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}