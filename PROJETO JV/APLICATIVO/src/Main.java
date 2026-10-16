import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // As mesas, horários e reservas de outros clientes são gerados
        // automaticamente pela classe Restaurante. Basta informar o nome.
        ArrayList<Restaurante> listaDeRestaurantes = new ArrayList<>();
        listaDeRestaurantes.add(new Restaurante("Sabor POO (Massas e Vinhos)"));
        listaDeRestaurantes.add(new Restaurante("Java Grill (Churrascaria)"));
        listaDeRestaurantes.add(new Restaurante("Sushi Byte (Comida Japonesa)"));

        System.out.println("====================================");
        System.out.println("    BEM-VINDO AO APP DE RESERVAS    ");
        System.out.println("====================================");

        // 1. Cadastro do cliente
        System.out.println("\nPor favor, realize seu cadastro para continuar:");
        String nome = lerTexto(scanner, "Digite seu nome: ");
        String telefone = lerTexto(scanner, "Digite seu telefone: ");

        Cliente clienteAtivo = new Cliente(nome, telefone);
        System.out.println("\nCadastro realizado com sucesso, " + clienteAtivo.getNome() + "!");

        // 2. Menu interativo
        int opcao = 0;
        while (opcao != 3) {
            System.out.println("\n============== MENU ==============");
            System.out.println("1. Ver Restaurantes e Fazer Reserva");
            System.out.println("2. Acompanhar minha posição na fila");
            System.out.println("3. Sair");
            System.out.println("==================================");
            opcao = lerInteiro(scanner, "Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    fazerReserva(scanner, listaDeRestaurantes, clienteAtivo);
                    break;
                case 2:
                    acompanharFila(listaDeRestaurantes, clienteAtivo);
                    break;
                case 3:
                    System.out.println("Saindo do aplicativo... Até logo, " + clienteAtivo.getNome() + "!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
        scanner.close();
    }

    private static void fazerReserva(Scanner scanner, ArrayList<Restaurante> restaurantes, Cliente cliente) {
        System.out.println("\n--- RESTAURANTES CADASTRADOS ---");
        for (int i = 0; i < restaurantes.size(); i++) {
            System.out.println((i + 1) + ". " + restaurantes.get(i).getNome());
        }

        int escolha = lerInteiro(scanner, "\nEscolha o número do restaurante: ");
        if (escolha < 1 || escolha > restaurantes.size()) {
            System.out.println("Opção de restaurante inválida.");
            return;
        }
        Restaurante restaurante = restaurantes.get(escolha - 1);
        System.out.println("\nBem-vindo à página do restaurante: " + restaurante.getNome());

        // O usuário escolhe o horário pelo número, sem precisar digitar "19:00" exato
        ArrayList<String> horarios = restaurante.getHorariosDisponiveis();
        restaurante.exibirHorarios();
        int escolhaHorario = lerInteiro(scanner, "\nEscolha o número do horário: ");
        if (escolhaHorario < 1 || escolhaHorario > horarios.size()) {
            System.out.println("Horário inválido.");
            return;
        }
        String horario = horarios.get(escolhaHorario - 1);

        // Mapa de mesas mostrado para o horário escolhido
        restaurante.exibirMapaMesas(horario);

        int qtd = lerInteiro(scanner, "\nSua mesa será para quantas pessoas? ");
        restaurante.solicitarMesa(cliente, qtd, horario);
    }

    private static void acompanharFila(ArrayList<Restaurante> restaurantes, Cliente cliente) {
        System.out.println("\n--- STATUS DA FILA ---");
        boolean estaNaFila = false;
        for (Restaurante r : restaurantes) {
            int posicao = r.acompanharFila(cliente);
            if (posicao > 0) {
                System.out.println("No restaurante '" + r.getNome() + "', você é o número " + posicao
                        + " na fila das " + r.getHorarioNaFila(cliente) + ".");
                estaNaFila = true;
            }
        }
        if (!estaNaFila) {
            System.out.println("Você não está aguardando na fila de nenhum restaurante no momento.");
        }
    }

    // Lê um número sem derrubar o programa se o usuário digitar letras
    private static int lerInteiro(Scanner scanner, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido.");
            }
        }
    }

    private static String lerTexto(Scanner scanner, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) return texto;
            System.out.println("Este campo não pode ficar vazio.");
        }
    }
}