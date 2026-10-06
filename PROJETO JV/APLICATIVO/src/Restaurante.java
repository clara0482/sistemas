import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

public class Restaurante {
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String nome;
    private ArrayList<Mesa> mesas;
    private ArrayList<Reserva> reservas;
    private FilaEspera filaEspera;
    private ArrayList<LocalTime> horariosDisponiveis;

    public Restaurante(String nome) {
        this.nome = nome;
        this.mesas = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.filaEspera = new FilaEspera();
        this.horariosDisponiveis = new ArrayList<>();
        gerarDadosAleatorios();
    }

    // Método que usa Random para gerar dados realistas para a apresentação
    private void gerarDadosAleatorios() {
        Random rand = new Random();

        // Gera entre 4 a 8 mesas para o restaurante
        int numMesas = rand.nextInt(5) + 4;
        for (int i = 1; i <= numMesas; i++) {
            // Capacidade será 2, 4, 6 ou 8 pessoas
            int capacidade = (rand.nextInt(4) + 1) * 2;
            Mesa novaMesa = new Mesa(i, capacidade);

            // Simula que algumas mesas (aprox. 40%) já estão ocupadas por outras pessoas
            if (rand.nextDouble() < 0.40) {
                novaMesa.ocupar();
            }
            mesas.add(novaMesa);
        }

        // Gera horários disponíveis aleatórios
        LocalTime[] todosHorarios = {
            LocalTime.of(18, 0),
            LocalTime.of(19, 0),
            LocalTime.of(19, 30),
            LocalTime.of(20, 0),
            LocalTime.of(20, 30),
            LocalTime.of(21, 0),
            LocalTime.of(22, 0)
        };
        for (LocalTime horario : todosHorarios) {
            // 60% de chance do horário estar liberado hoje
            if (rand.nextDouble() < 0.60) {
                horariosDisponiveis.add(horario);
            }
        }
        // Garante que tenha pelo menos um horário caso o random zere tudo
        if (horariosDisponiveis.isEmpty()) {
            horariosDisponiveis.add(LocalTime.of(20, 0));
        }
    }

    public void exibirOpcoesDisponiveis() {
        System.out.println("\n--- HORÁRIOS DISPONÍVEIS HOJE ---");
        for (LocalTime h : horariosDisponiveis) {
            System.out.print("[" + h.format(FORMATO_HORA) + "] ");
        }
        System.out.println("\n\n--- MAPA DE MESAS DO LOCAL ---");
        for (Mesa m : mesas) {
            String status = m.isDisponivel() ? "🟢 Livre" : "🔴 Ocupada";
            System.out.println("Mesa " + m.getNumero() + " | Capacidade: " + m.getCapacidade() + " pessoas | Status: " + status);
        }
        System.out.println("---------------------------------");
    }

    public String getNome() { return nome; }

    public boolean solicitarMesa(Cliente cliente, int quantidadePessoas, LocalDateTime horario) {
        // Validação se o horário digitado existe (compara só a parte da hora)
        if (!horariosDisponiveis.contains(horario.toLocalTime())) {
            System.out.println(" O horário " + horario.format(FORMATO_HORA) + " não está disponível. Reserva cancelada.");
            return false;
        }

        for (Mesa mesa : mesas) {
            if (mesa.isDisponivel() && mesa.getCapacidade() >= quantidadePessoas) {
                Reserva novaReserva = new Reserva(cliente, mesa, horario);
                reservas.add(novaReserva);
                System.out.println("\nSUCESSO! Mesa " + mesa.getNumero() + " (para até " + mesa.getCapacidade()
                        + " pessoas) reservada no " + this.nome + " em " + horario.format(FORMATO_DATA_HORA));
                return true;
            }
        }
        System.out.println("\nNenhuma mesa compatível ou livre neste momento no " + this.nome + ".");
        System.out.println("Você foi redirecionado para a fila de espera.");
        filaEspera.entrarFila(cliente, quantidadePessoas);
        return false;
    }

    public int acompanharFila(Cliente cliente) {
        return filaEspera.acompanharPosicao(cliente);
    }
}