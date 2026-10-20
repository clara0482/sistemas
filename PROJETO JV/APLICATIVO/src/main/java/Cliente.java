import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String telefone;

    protected Cliente() {
    }

    public Cliente(String nome, String telefone) {
        setNome(nome);
        setTelefone(telefone);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do cliente.");
        }
        this.nome = nome.trim();
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        if (telefone == null) {
            throw new IllegalArgumentException("Informe o telefone do cliente.");
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() != 10 && digitos.length() != 11) {
            throw new IllegalArgumentException("O telefone deve ter 10 ou 11 dígitos, incluindo o DDD.");
        }
        this.telefone = digitos;
    }

}
