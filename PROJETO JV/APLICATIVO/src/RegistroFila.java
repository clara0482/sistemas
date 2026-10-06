public class RegistroFila {
    private Cliente cliente;
    private int quantidadePessoas;
    private boolean avisoPrevioEnviado; //nao acho que faz sentido essa logica de usar um boolean para isso, acho que se tivesse apenas um metodo seria o suficiente 

    public RegistroFila(Cliente cliente, int quantidadePessoas) {
        this.cliente = cliente;
        this.quantidadePessoas = quantidadePessoas;
        this.avisoPrevioEnviado = false;
    }

    public void enviarAviso() {
        this.avisoPrevioEnviado = true;
        System.out.println("Aviso: " + cliente.getNome() + ", sua mesa está quase pronta!");
    }

    public Cliente getCliente() { return cliente; }
    public int getQuantidadePessoas() { return quantidadePessoas; }
    public boolean isAvisoPrevioEnviado() { return avisoPrevioEnviado; }
}