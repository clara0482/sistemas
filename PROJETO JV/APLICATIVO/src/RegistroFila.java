public class RegistroFila {
    private Cliente cliente;
    private int quantidadePessoas;
    private String horario;
    private boolean avisoPrevioEnviado;

    public RegistroFila(Cliente cliente, int quantidadePessoas, String horario) {
        this.cliente = cliente;
        this.quantidadePessoas = quantidadePessoas;
        this.horario = horario;
        this.avisoPrevioEnviado = false;
    }

    public void enviarAviso() {
        this.avisoPrevioEnviado = true;
        System.out.println("Aviso: " + cliente.getNome() + ", sua mesa está quase pronta!");
    }

    public Cliente getCliente() { return cliente; }
    public int getQuantidadePessoas() { return quantidadePessoas; }
    public String getHorario() { return horario; }
    public boolean isAvisoPrevioEnviado() { return avisoPrevioEnviado; }
}