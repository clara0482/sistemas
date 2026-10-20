import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "mesa", uniqueConstraints = @UniqueConstraint(columnNames = {"restaurante_id", "numero"}))
public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false)
    private int capacidade;

    protected Mesa() {
    }

    public Mesa(Restaurante restaurante, int numero, int capacidade) {
        if (restaurante == null || numero < 1 || capacidade < 1 || capacidade > 8) {
            throw new IllegalArgumentException("Informe restaurante, número e capacidade válidos para a mesa.");
        }
        this.restaurante = restaurante;
        this.numero = numero;
        this.capacidade = capacidade;
        restaurante.registrarMesa(this);
    }

    public boolean comporta(int pessoas) {
        return pessoas > 0 && pessoas <= capacidade;
    }

    public Long getId() {
        return id;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public int getNumero() {
        return numero;
    }

    public int getCapacidade() {
        return capacidade;
    }
}
