import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static String lerTexto(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Digite um texto para continuar.");
        }
    }

    private static String lerTelefone() {
        while (true) {
            String telefone = lerTexto("Telefone com DDD: ").replaceAll("\\D", "");
            if (telefone.length() == 10 || telefone.length() == 11) {
                return telefone;
            }
            System.out.println("Informe 10 ou 11 dígitos, incluindo o DDD.");
        }
    }

    private static int lerInteiro(String mensagem, int minimo, int maximo) {
        String mensagemErro = "Digite um número entre " + minimo + " e " + maximo + ".";
        while (true) {
            try {
                int valor = Integer.parseInt(lerTexto(mensagem));
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
            } catch (NumberFormatException e) {
            }
            System.out.println(mensagemErro);
        }
    }

    static LocalDateTime interpretarHorario(String data, String hora) {
        LocalDate dia = LocalDate.parse(data, DATA);
        LocalTime horario = LocalTime.parse(hora);
        LocalDateTime escolhido = LocalDateTime.of(dia, horario);
        if (!escolhido.isAfter(LocalDateTime.now()) || !SistemaReservas.HORARIOS.contains(horario)) {
            throw new IllegalArgumentException("Escolha um dos horários exibidos em uma data futura.");
        }
        return escolhido;
    }

    private static LocalDateTime lerHorario() {
        while (true) {
            try {
                String data = lerTexto("Data (dd/MM/yyyy): ");
                String hora = lerTexto("Horário (HH:mm): ");
                return interpretarHorario(data, hora);
            } catch (DateTimeParseException e) {
                System.out.println("Data ou horário inválido. Tente novamente.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static Restaurante escolherRestaurante(SistemaReservas sistema) {
        List<Restaurante> restaurantes = sistema.listarRestaurantes();
        if (restaurantes.isEmpty()) {
            System.out.println("Nenhum restaurante cadastrado.");
            return null;
        }
        System.out.println("\n--- RESTAURANTES ---");
        for (int i = 0; i < restaurantes.size(); i++) {
            System.out.println((i + 1) + ". " + restaurantes.get(i).getNome());
        }
        int escolha = lerInteiro("Escolha o restaurante: ", 1, restaurantes.size());
        return restaurantes.get(escolha - 1);
    }

    private static void mostrarMesas(SistemaReservas sistema, Restaurante restaurante, LocalDateTime horario) {
        System.out.println("\n--- MESAS EM " + horario.format(DATA_HORA) + " ---");
        for (Map.Entry<Mesa, Boolean> item : sistema.listarMesas(restaurante.getId(), horario).entrySet()) {
            Mesa mesa = item.getKey();
            System.out.println("Mesa " + mesa.getNumero() + " | " + mesa.getCapacidade() + " pessoas | "
                    + (item.getValue() ? "Livre" : "Reservada"));
        }
    }

    private static void fazerReserva(SistemaReservas sistema, Cliente cliente) {
        Restaurante restaurante = escolherRestaurante(sistema);
        if (restaurante == null) {
            return;
        }
        System.out.println("Horários: " + SistemaReservas.HORARIOS);
        LocalDateTime horario = lerHorario();
        mostrarMesas(sistema, restaurante, horario);
        int pessoas = lerInteiro("Quantidade de pessoas: ", 1, 8);
        Reserva reserva = sistema.solicitarMesa(restaurante.getId(), cliente.getId(), pessoas, horario);
        if (reserva == null) {
            System.out.println("Sem mesa disponível. Sua solicitação está na fila de espera.");
        } else {
            System.out.println("Reserva #" + reserva.getId() + " confirmada na mesa " + reserva.getMesa().getNumero()
                    + " para " + reserva.getQuantidadePessoas() + " pessoas.");
        }
    }

    private static void mostrarReservas(SistemaReservas sistema, Cliente cliente) {
        List<Reserva> reservas = sistema.listarReservas(cliente.getId());
        if (reservas.isEmpty()) {
            System.out.println("Você ainda não tem reservas.");
        }
        for (Reserva reserva : reservas) {
            String pessoas = reserva.getQuantidadePessoas() == null
                    ? "Quantidade não informada (reserva antiga)" : reserva.getQuantidadePessoas() + " pessoas";
            System.out.println("#" + reserva.getId() + " | " + reserva.getMesa().getRestaurante().getNome()
                    + " | Mesa " + reserva.getMesa().getNumero() + " | "
                    + reserva.getHorario().format(DATA_HORA) + " | " + pessoas + " | " + reserva.getStatus());
        }
    }

    private static void cancelarReserva(SistemaReservas sistema, Cliente cliente) {
        mostrarReservas(sistema, cliente);
        long reservaId = lerInteiro("Número da reserva para cancelar: ", 1, Integer.MAX_VALUE);
        if (sistema.cancelarReserva(reservaId, cliente.getId())) {
            System.out.println("Reserva cancelada.");
        } else {
            System.out.println("Reserva não encontrada ou já encerrada.");
        }
    }

    private static void alterarReserva(SistemaReservas sistema, Cliente cliente) {
        mostrarReservas(sistema, cliente);
        long reservaId = lerInteiro("Número da reserva ativa para alterar: ", 1, Integer.MAX_VALUE);
        int pessoas = lerInteiro("Nova quantidade de pessoas: ", 1, 8);
        if (sistema.alterarReserva(reservaId, cliente.getId(), pessoas)) {
            System.out.println("Reserva alterada.");
            mostrarReservas(sistema, cliente);
        } else {
            System.out.println("Sem mesa disponível para a troca, considerando a prioridade da fila. "
                    + "A reserva original foi mantida.");
        }
    }

    private static void mostrarFila(SistemaReservas sistema, Cliente cliente) {
        Map<RegistroFila, Integer> posicoes = sistema.consultarFila(cliente.getId());
        if (posicoes.isEmpty()) {
            System.out.println("Você não está na fila de espera.");
        } else {
            System.out.println("Posição entre pedidos com períodos sobrepostos no mesmo restaurante. "
                    + "O atendimento também depende da capacidade das mesas.");
        }
        for (Map.Entry<RegistroFila, Integer> item : posicoes.entrySet()) {
            RegistroFila registro = item.getKey();
            System.out.println(registro.getRestaurante().getNome() + " | "
                    + registro.horarioDesejado().format(DATA_HORA) + ": posição " + item.getValue());
        }
    }

    private static void executarMenu(SistemaReservas sistema, Cliente cliente) {
        int opcao;
        do {
            System.out.println("\n1. Fazer reserva\n2. Minhas reservas\n3. Cancelar reserva\n4. Acompanhar fila"
                    + "\n5. Alterar reserva\n6. Sair");
            opcao = lerInteiro("Opção: ", 1, 6);
            try {
                switch (opcao) {
                    case 1: {
                        fazerReserva(sistema, cliente);
                        break;
                    }
                    case 2: {
                        mostrarReservas(sistema, cliente);
                        break;
                    }
                    case 3: {
                        cancelarReserva(sistema, cliente);
                        break;
                    }
                    case 4: {
                        mostrarFila(sistema, cliente);
                        break;
                    }
                    case 5: {
                        alterarReserva(sistema, cliente);
                        break;
                    }
                    case 6: {
                        System.out.println("Aplicativo encerrado.");
                        break;
                    }
                }
            } catch (RuntimeException e) {
                System.err.println("Não foi possível concluir a operação: " + e.getMessage());
            }
        } while (opcao != 6);
    }

    public static void main(String[] args) {
        try (scanner) {
            SistemaReservas sistema = new SistemaReservas();
            sistema.cadastrarRestaurantesIniciais();
            System.out.println("=== RESERVAS DE RESTAURANTES ===");
            Cliente cliente = sistema.cadastrarCliente(lerTexto("Nome: "), lerTelefone());
            System.out.println("Bem-vindo, " + cliente.getNome() + "!");
            executarMenu(sistema, cliente);
        } catch (RuntimeException e) {
            System.out.println("Não foi possível iniciar o aplicativo: " + e.getMessage());
        } finally {
            HibernateUtil.fechar();
        }
    }
}
