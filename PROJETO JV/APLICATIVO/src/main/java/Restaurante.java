import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "restaurante")
public class Restaurante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @OneToMany(mappedBy = "restaurante")
    @OrderBy("capacidade ASC, numero ASC")
    private List<Mesa> mesas = new ArrayList<>();

    protected Restaurante() {
    }

    public Restaurante(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do restaurante.");
        }
        this.nome = nome.trim();
    }

    public Mesa adicionarMesa(int numero, int capacidade) {
        return new Mesa(this, numero, capacidade);
    }

    void registrarMesa(Mesa mesa) {
        if (mesa == null || (mesa.getRestaurante() != this
                && (id == null || !id.equals(mesa.getRestaurante().getId())))) {
            throw new IllegalArgumentException("A mesa deve pertencer a este restaurante.");
        }
        if (buscarMesa(mesa.getNumero()) != null) {
            throw new IllegalArgumentException("Já existe uma mesa com este número no restaurante.");
        }
        mesas.add(mesa);
        mesas.sort(Comparator.comparingInt(Mesa::getCapacidade).thenComparingInt(Mesa::getNumero));
    }

    public Mesa buscarMesa(int numero) {
        for (Mesa mesa : mesas) {
            if (mesa.getNumero() == numero) {
                return mesa;
            }
        }
        return null;
    }

    public List<Mesa> getMesas() {
        return Collections.unmodifiableList(mesas);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
