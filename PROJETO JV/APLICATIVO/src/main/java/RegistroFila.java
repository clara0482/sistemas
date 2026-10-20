import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "registro_fila")
public class RegistroFila {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private int quantidadePessoas;

    @Column(nullable = false)
    private LocalDateTime horarioDesejado;

    protected RegistroFila() {
    }

    public RegistroFila(Restaurante restaurante, Cliente cliente, int quantidadePessoas, LocalDateTime horarioDesejado) {
        if (restaurante == null || cliente == null || horarioDesejado == null
                || quantidadePessoas < 1 || quantidadePessoas > 8) {
            throw new IllegalArgumentException("Informe restaurante, cliente, pessoas e horário válidos para a fila.");
        }
        this.restaurante = restaurante;
        this.cliente = cliente;
        this.quantidadePessoas = quantidadePessoas;
        this.horarioDesejado = horarioDesejado;
    }

    public boolean podeOcupar(Mesa mesa, LocalDateTime horarioLiberado) {
        if (mesa == null || horarioLiberado == null) {
            return false;
        }
        boolean mesmoRestaurante = restaurante == mesa.getRestaurante()
                || (restaurante.getId() != null && restaurante.getId().equals(mesa.getRestaurante().getId()));
        if (!mesmoRestaurante || !mesa.comporta(quantidadePessoas)
                || !horarioDesejado.isAfter(LocalDateTime.now())) {
            return false;
        }
        return Reserva.horariosSobrepostos(horarioDesejado, horarioLiberado);
    }

    public Long getId() {
        return id;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public LocalDateTime horarioDesejado() {
        return horarioDesejado;
    }

    public Cliente clienteDaFila() {
        return cliente;
    }

    public int getQuantidadePessoas() {
        return quantidadePessoas;
    }
}
