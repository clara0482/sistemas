import java.time.LocalDateTime;

public class Reserva {
    private Cliente cliente;
    private Mesa mesa;
    private LocalDateTime horario;
    private String status; // ATIVA, CONCLUIDA, CANCELADA //pode usar um obj

    public Reserva(Cliente cliente, Mesa mesa, LocalDateTime horario) {
        this.cliente = cliente;
        this.mesa = mesa;
        this.horario = horario;
        this.status = "ATIVA";
        this.mesa.ocupar(); // Regra de negócio: vincula ocupação imediata ao criar reserva
    }

    public void finalizarReserva() {
        this.status = "CONCLUIDA";
        this.mesa.liberar();
    }

    public Cliente getCliente() { return cliente; }
    public Mesa getMesa() { return mesa; }
    public LocalDateTime getHorario() { return horario; }
    public String getStatus() { return status; }
}