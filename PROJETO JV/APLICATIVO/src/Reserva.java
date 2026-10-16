public class Reserva {
    private Cliente cliente;
    private Mesa mesa;
    private String horario;
    private StatusReserva status;

    public Reserva(Cliente cliente, Mesa mesa, String horario) {
        this.cliente = cliente;
        this.mesa = mesa;
        this.horario = horario;
        this.status = StatusReserva.ATIVA;
    }

    // mesa volta a ficar livre naquele horario automaticamente, pq so reservas ATIVAS bloqueiam a mesa.
    public void finalizarReserva() {
        this.status = StatusReserva.CONCLUIDA;
    }

    public boolean isAtiva() { return status == StatusReserva.ATIVA; }

    public Cliente getCliente() { return cliente; }
    public Mesa getMesa() { return mesa; }
    public String getHorario() { return horario; }
    public StatusReserva getStatus() { return status; }
}