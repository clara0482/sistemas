import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    // Texto obrigatorio: IllegalArgumentException se vazio
    private static String lerTextoObrigatorio(String mensagem, String nomeCampo) {
        while (true) {
            System.out.print(mensagem);
            try {
                String texto = scanner.nextLine().trim();
                if (texto.isEmpty()) {
                    throw new IllegalArgumentException(nomeCampo + " nao pode ficar vazio.");
                }
                return texto;
            } catch (IllegalArgumentException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }

    // Telefone: IllegalArgumentException se nao tiver 10 ou 11 digitos
    private static String lerTelefone(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                String texto = scanner.nextLine().trim();
                String digitos = texto.replaceAll("\\D", "");
                if (digitos.length() < 10 || digitos.length() > 11) {
                    throw new IllegalArgumentException("Telefone invalido. Informe DDD + numero (10 ou 11 digitos).");
                }
                return texto;
            } catch (IllegalArgumentException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }

    // Inteiro dentro de um intervalo:
    // NumberFormatException (nao e numero) e IllegalArgumentException (fora do intervalo)
    private static int lerInteiro(String mensagem, int minimo, int maximo) {
        while (true) {
            System.out.print(mensagem);
            try {
                int valor = Integer.parseInt(scanner.nextLine().trim());
                if (valor < minimo || valor > maximo) {
                    throw new IllegalArgumentException("Digite um numero entre " + minimo + " e " + maximo + ".");
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("ERRO: Digite apenas numeros inteiros.");
            } catch (IllegalArgumentException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }

    // Horario: DateTimeParseException se nao estiver no formato HH:mm
    private static LocalDateTime lerHorario(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                LocalTime hora = LocalTime.parse(scanner.nextLine().trim());
                return LocalDateTime.of(LocalDate.now(), hora);
            } catch (DateTimeParseException e) {
                System.out.println("ERRO: Horario invalido. Use o formato HH:mm com dois digitos (ex.: 20:30).");
            }
        }
    }

    public static void main(String[] args) {

        ArrayList<Restaurante> listaDeRestaurantes = new ArrayList<>();
        listaDeRestaurantes.add(new Restaurante("Sabor POO (Massas e Vinhos)"));
        listaDeRestaurantes.add(new Restaurante("Java Grill (Churrascaria)"));
        listaDeRestaurantes.add(new Restaurante("Sushi Byte (Comida Japonesa)"));
        // =================================================================

        System.out.println("====================================");
        System.out.println("    BEM-VINDO AO APP DE RESERVAS    ");
        System.out.println("====================================");

        // 1. O CLIENTE SE CADASTRA PRIMEIRO
        System.out.println("\nPor favor, realize seu cadastro para continuar:");
        String nome = lerTextoObrigatorio("Digite seu nome: ", "O nome");
        String telefone = lerTelefone("Digite seu telefone (com DDD): ");

        Cliente clienteAtivo = new Cliente(nome, telefone);
        System.out.println("\nCadastro realizado com sucesso, " + clienteAtivo.getNome() + "!");

        // 2. MENU INTERATIVO DO APLICATIVO
        int opcao = 0;
        while (opcao != 3) {
            System.out.println("\n============== MENU ==============");
            System.out.println("1. Ver Restaurantes e Fazer Reserva");
            System.out.println("2. Acompanhar minha posicao na fila");
            System.out.println("3. Sair");
            System.out.println("==================================");
            opcao = lerInteiro("Escolha uma opcao: ", 1, 3);

            switch (opcao) {
                case 1:
                    System.out.println("\n--- RESTAURANTES CADASTRADOS ---");
                    for (int i = 0; i < listaDeRestaurantes.size(); i++) {
                        System.out.println((i + 1) + ". " + listaDeRestaurantes.get(i).getNome());
                    }

                    int escolhaRestaurante = lerInteiro("\nEscolha o numero do restaurante: ",
                            1, listaDeRestaurantes.size());
                    Restaurante restauranteEscolhido = listaDeRestaurantes.get(escolhaRestaurante - 1);

                    System.out.println("\nBem-vindo a pagina do restaurante: " + restauranteEscolhido.getNome());
                    // O app mostra as opcoes antes do usuario pedir
                    restauranteEscolhido.exibirOpcoesDisponiveis();

                    int qtd = lerInteiro("\nSua mesa sera para quantas pessoas? ", 1, 8);
                    LocalDateTime horario = lerHorario("Qual o horario desejado (formato HH:mm, como mostrado acima)? ");

                    // O sistema processa a reserva e vincula a uma mesa livre daquele tamanho
                    restauranteEscolhido.solicitarMesa(clienteAtivo, qtd, horario);
                    break;

                case 2:
                    System.out.println("\n--- STATUS DA FILA ---");
                    boolean estaNaFila = false;
                    for (Restaurante r : listaDeRestaurantes) {
                        int posicao = r.acompanharFila(clienteAtivo);
                        if (posicao > 0) {
                            System.out.println("No restaurante '" + r.getNome() + "', voce e o numero " + posicao + " na fila.");
                            estaNaFila = true;
                        }
                    }
                    if (!estaNaFila) {
                        System.out.println("Voce nao esta aguardando na fila de nenhum restaurante no momento.");
                    }
                    break;

                case 3:
                    System.out.println("Saindo do aplicativo... Ate logo, " + clienteAtivo.getNome() + "!");
                    break;
            }
        }
        scanner.close();
    }
}