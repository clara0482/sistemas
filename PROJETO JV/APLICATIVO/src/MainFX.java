import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

/**
 * Interface gráfica (JavaFX) do app de reservas.
 * Reaproveita as classes do projeto (Cliente, Restaurante, Mesa, Reserva, FilaEspera)
 * sem alterar nenhuma regra de negócio. A versão console continua em Main.java.
 */
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
    private Cliente clienteAtivo;
    private Stage stage;

    // Componentes da tela de reserva
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

        // Mesmos restaurantes do Main.java (as mesas e horários são gerados com Random)
        restaurantes.add(new Restaurante("Sabor POO (Massas e Vinhos)"));
        restaurantes.add(new Restaurante("Java Grill (Churrascaria)"));
        restaurantes.add(new Restaurante("Sushi Byte (Comida Japonesa)"));

        stage.setTitle("App de Reservas");
        mostrarTelaCadastro();
        stage.show();
    }

    // ============================================================
    // TELA 1: CADASTRO DO CLIENTE
    // ============================================================
    private void mostrarTelaCadastro() {
        Label titulo = new Label("Bem-vindo ao App de Reservas");
        titulo.getStyleClass().add("titulo");
        Label sub = new Label("Realize seu cadastro para continuar");
        sub.getStyleClass().add("subtitulo");

        TextField txtNome = new TextField();
        txtNome.setPromptText("Seu nome");
        TextField txtTelefone = new TextField();
        txtTelefone.setPromptText("Seu telefone");

        Button btnEntrar = new Button("Entrar");
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
            clienteAtivo = new Cliente(nome, telefone);
            mostrarTelaPrincipal();
        });

        VBox card = new VBox(12, titulo, sub, new Label("Nome"), txtNome, new Label("Telefone"), txtTelefone, btnEntrar);
        card.getStyleClass().add("card");
        card.setMaxWidth(380);

        StackCentro raiz = new StackCentro(card);
        trocarCena(raiz, 700, 500);
    }

    // ============================================================
    // TELA 2: PRINCIPAL (abas: Restaurantes / Minha fila)
    // ============================================================
    private void mostrarTelaPrincipal() {
        Label ola = new Label("Olá, " + clienteAtivo.getNome() + "!");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        Button btnSair = new Button("Sair");
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

        TabPane abas = new TabPane(abaReserva, abaFila);
        abas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) -> {
            if (nova == abaFila) {
                atualizarFila();
            }
        });

        BorderPane raiz = new BorderPane();
        raiz.setTop(header);
        raiz.setCenter(abas);
        trocarCena(raiz, 980, 620);
    }

    // ---------- Aba 1: escolher restaurante e reservar ----------
    private Region construirAbaReserva() {
        // Lista de restaurantes (lado esquerdo)
        Label lblRest = new Label("Restaurantes");
        lblRest.getStyleClass().add("secao");
        ListView<Restaurante> listaRestaurantes = new ListView<>(FXCollections.observableArrayList(restaurantes));
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

        // Detalhes do restaurante (lado direito)
        lblNomeRestaurante = new Label("Selecione um restaurante");
        lblNomeRestaurante.getStyleClass().add("titulo");

        cbHorarios = new ComboBox<>();
        cbHorarios.setPromptText("Escolha o horário");
        cbHorarios.setPrefWidth(180);

        tabelaMesas = new TableView<>();
        tabelaMesas.setPlaceholder(new Label("Escolha um restaurante para ver o mapa de mesas"));
        tabelaMesas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Mesa, Number> colNumero = new TableColumn<>("Mesa");
        colNumero.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumero()));

        TableColumn<Mesa, Number> colCapacidade = new TableColumn<>("Capacidade (pessoas)");
        colCapacidade.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCapacidade()));

        TableColumn<Mesa, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isDisponivel() ? "Livre" : "Ocupada"));
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
        tabelaMesas.getColumns().addAll(colNumero, colCapacidade, colStatus);
        VBox.setVgrow(tabelaMesas, Priority.ALWAYS);

        spQuantidade = new Spinner<>(1, 20, 2);
        spQuantidade.setEditable(true);
        spQuantidade.setPrefWidth(90);

        btnReservar = new Button("Reservar mesa");
        btnReservar.getStyleClass().add("botao");
        btnReservar.setDisable(true);
        btnReservar.setOnAction(e -> reservar());

        HBox linhaReserva = new HBox(12,
                new Label("Pessoas:"), spQuantidade,
                new Label("Horário:"), cbHorarios,
                btnReservar);
        linhaReserva.setAlignment(Pos.CENTER_LEFT);

        Label lblMesas = new Label("Mapa de mesas do local");
        lblMesas.getStyleClass().add("secao");

        VBox direita = new VBox(12, lblNomeRestaurante, lblMesas, tabelaMesas, linhaReserva);
        direita.getStyleClass().add("card");
        HBox.setHgrow(direita, Priority.ALWAYS);

        listaRestaurantes.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                selecionarRestaurante(novo);
            }
        });

        HBox conteudo = new HBox(16, esquerda, direita);
        conteudo.setPadding(new Insets(16));
        return conteudo;
    }

    private void selecionarRestaurante(Restaurante r) {
        restauranteSelecionado = r;
        lblNomeRestaurante.setText(r.getNome());
        cbHorarios.setItems(FXCollections.observableArrayList(r.getHorariosDisponiveis()));
        cbHorarios.getSelectionModel().clearSelection();
        tabelaMesas.setItems(FXCollections.observableArrayList(r.getMesas()));
        btnReservar.setDisable(false);
    }

    private void reservar() {
        if (restauranteSelecionado == null) {
            return;
        }
        String horario = cbHorarios.getValue();
        if (horario == null) {
            alerta(Alert.AlertType.WARNING, "Reserva", "Escolha um horário disponível.");
            return;
        }

        int quantidade;
        try {
            // Lê o texto do editor para considerar números digitados à mão no Spinner
            quantidade = Integer.parseInt(spQuantidade.getEditor().getText().trim());
            if (quantidade < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            alerta(Alert.AlertType.WARNING, "Reserva", "Informe uma quantidade de pessoas válida.");
            return;
        }

        // Regra de negócio permanece na classe Restaurante
        boolean reservou = restauranteSelecionado.solicitarMesa(clienteAtivo, quantidade, horario);

        if (reservou) {
            ArrayList<Reserva> reservas = restauranteSelecionado.getReservas();
            Mesa mesa = reservas.get(reservas.size() - 1).getMesa();
            alerta(Alert.AlertType.INFORMATION, "Reserva confirmada",
                    "Mesa " + mesa.getNumero() + " (até " + mesa.getCapacidade() + " pessoas)\n"
                            + "reservada em " + restauranteSelecionado.getNome() + " às " + horario + ".");
        } else {
            alerta(Alert.AlertType.WARNING, "Sem mesa disponível",
                    "Nenhuma mesa compatível ou livre neste momento em " + restauranteSelecionado.getNome() + ".\n"
                            + "Você foi redirecionado para a fila de espera.");
        }
        tabelaMesas.refresh();
    }

    // ---------- Aba 2: acompanhar fila ----------
    private Region construirAbaFila() {
        Label titulo = new Label("Status da fila");
        titulo.getStyleClass().add("secao");

        listaFila = new ListView<>();
        Label vazio = new Label("Você não está aguardando na fila de nenhum restaurante no momento.");
        vazio.getStyleClass().add("horario-vazio");
        listaFila.setPlaceholder(vazio);
        VBox.setVgrow(listaFila, Priority.ALWAYS);

        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.getStyleClass().add("botao");
        btnAtualizar.setOnAction(e -> atualizarFila());

        VBox card = new VBox(12, titulo, listaFila, btnAtualizar);
        card.getStyleClass().add("card");

        VBox conteudo = new VBox(card);
        conteudo.setPadding(new Insets(16));
        VBox.setVgrow(card, Priority.ALWAYS);
        return conteudo;
    }

    private void atualizarFila() {
        listaFila.getItems().clear();
        for (Restaurante r : restaurantes) {
            int posicao = r.acompanharFila(clienteAtivo);
            if (posicao > 0) {
                String texto = "No restaurante '" + r.getNome() + "', você é o número " + posicao + " na fila.";
                if (posicao <= 2) {
                    texto += "  Aviso: sua mesa está quase pronta!";
                }
                listaFila.getItems().add(texto);
            }
        }
    }

    // ============================================================
    // Utilitários
    // ============================================================
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

    /** Painel simples que centraliza um nó no meio da tela. */
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