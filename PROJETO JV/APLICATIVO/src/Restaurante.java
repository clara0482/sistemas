import java.util.ArrayList;
import java.util.Random;

public class Restaurante {
    private String nome;
    private ArrayList<Mesa> mesas; 
    private ArrayList<Reserva> reservas; 
    private FilaEspera filaEspera;
    private ArrayList<String> horariosDisponiveis; // Nova coleção para horários

    public Restaurante(String nome) {
        this.nome = nome;
        this.mesas = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.filaEspera = new FilaEspera();
        this.horariosDisponiveis = new ArrayList<>();
        gerarDadosAleatorios(); // Gera o cenário dinâmico
    }

    // Método que usa Random para gerar dados realistas para a apresentação
    private void gerarDadosAleatorios() {
        Random rand = new Random();
        
        // Gera entre 4 a 8 mesas para o restaurante
        int numMesas = rand.nextInt(5) + 4; 
        for(int i = 1; i <= numMesas; i++) {
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
        String[] todosHorarios = {"18:00", "19:00", "19:30", "20:00", "20:30", "21:00", "22:00"};
        for (String horario : todosHorarios) {
            // 60% de chance do horário estar liberado hoje
            if (rand.nextDouble() < 0.60) {
                horariosDisponiveis.add(horario);
            }
        }
        // Garante que tenha pelo menos um horário caso o random zere tudo
        if(horariosDisponiveis.isEmpty()) horariosDisponiveis.add("20:00"); 
    }

    public void exibirOpcoesDisponiveis() {
        System.out.println("\n--- HORÁRIOS DISPONÍVEIS HOJE ---");
        for (String h : horariosDisponiveis) {
            System.out.print("[" + h + "] ");
        }
        System.out.println("\n\n--- MAPA DE MESAS DO LOCAL ---");
        for (Mesa m : mesas) {
            String status = m.isDisponivel() ? "🟢 Livre" : "🔴 Ocupada";
            System.out.println("Mesa " + m.getNumero() + " | Capacidade: " + m.getCapacidade() + " pessoas | Status: " + status);
        }
        System.out.println("---------------------------------");
    }

    public String getNome() { return nome; }
    public ArrayList<Mesa> getMesas() { return mesas; }
    public ArrayList<String> getHorariosDisponiveis() { return horariosDisponiveis; }
    public ArrayList<Reserva> getReservas() { return reservas; }

    public boolean solicitarMesa(Cliente cliente, int quantidadePessoas, String horario) {
        // Validação se o horário digitado existe
        if (!horariosDisponiveis.contains(horario)) {
            System.out.println("⚠️ O horário " + horario + " não está disponível. Reserva cancelada.");
            return false;
        }

        for (Mesa mesa : mesas) {
            if (mesa.isDisponivel() && mesa.getCapacidade() >= quantidadePessoas) {
                Reserva novaReserva = new Reserva(cliente, mesa, horario);
                reservas.add(novaReserva);
                System.out.println("\n✅ SUCESSO! Mesa " + mesa.getNumero() + " (para até " + mesa.getCapacidade() + " pessoas) reservada no " + this.nome + " às " + horario);
                return true;
            }
        }
        System.out.println("\n⚠️ Nenhuma mesa compatível ou livre neste momento no " + this.nome + ".");
        System.out.println("Você foi redirecionado para a fila de espera.");
        filaEspera.entrarFila(cliente, quantidadePessoas);
        return false;
    }

    public int acompanharFila(Cliente cliente) {
        return filaEspera.acompanharPosicao(cliente);
    }
}