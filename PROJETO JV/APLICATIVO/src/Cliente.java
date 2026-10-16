import java.util.Objects;

public class Cliente {
    private String nome;
    private String telefone;

    public Cliente(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    // dois clientes são o mesmo se tiverem o mesmo telefone
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        Cliente outro = (Cliente) o;
        return Objects.equals(telefone, outro.telefone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(telefone);
    }
}