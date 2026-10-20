import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reserva")
public class Reserva {
    public static final int DURACAO_HORAS = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @Column(nullable = false)
    private LocalDateTime horario;

    private Integer quantidadePessoas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReserva status = StatusReserva.ATIVA;

    protected Reserva() {
    }

    public Reserva(Cliente cliente, Mesa mesa, LocalDateTime horario, int quantidadePessoas) {
        if (cliente == null || mesa == null || horario == null) {
            throw new IllegalArgumentException("A reserva deve ter cliente, mesa e horário.");
        }
        this.cliente = cliente;
        this.mesa = mesa;
        this.horario = horario;
        alterar(mesa, quantidadePessoas);
    }

    public static boolean horariosSobrepostos(LocalDateTime inicioA, LocalDateTime inicioB) {
        return inicioA.isBefore(inicioB.plusHours(DURACAO_HORAS))
                && inicioB.isBefore(inicioA.plusHours(DURACAO_HORAS));
    }

    public void alterar(Mesa novaMesa, int pessoas) {
        if (status != StatusReserva.ATIVA) {
            throw new IllegalStateException("A reserva não está ativa.");
        }
        if (novaMesa == null || !novaMesa.comporta(pessoas)) {
            throw new IllegalArgumentException("A mesa não comporta essa quantidade de pessoas.");
        }
        Restaurante atual = mesa.getRestaurante();
        Restaurante novo = novaMesa.getRestaurante();
        if (atual != novo && (atual.getId() == null || !atual.getId().equals(novo.getId()))) {
            throw new IllegalArgumentException("A nova mesa deve pertencer ao mesmo restaurante.");
        }
        mesa = novaMesa;
        quantidadePessoas = pessoas;
    }

    public void cancelar() {
        if (status != StatusReserva.ATIVA) {
            throw new IllegalStateException("A reserva não está ativa.");
        }
        status = StatusReserva.CANCELADA;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public LocalDateTime getHorario() {
        return horario;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public Integer getQuantidadePessoas() {
        return quantidadePessoas;
    }

}
