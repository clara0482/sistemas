import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilaEspera {
    private final Restaurante restaurante;
    private final List<RegistroFila> registros = new ArrayList<>();

    public FilaEspera(Restaurante restaurante, List<RegistroFila> registrosSalvos) {
        if (restaurante == null || registrosSalvos == null) {
            throw new IllegalArgumentException("Informe o restaurante e os registros da fila.");
        }
        this.restaurante = restaurante;
        for (RegistroFila registro : registrosSalvos) {
            adicionar(registro);
        }
    }

    public void adicionar(RegistroFila registro) {
        if (registro == null || !mesmoRestaurante(registro.getRestaurante())) {
            throw new IllegalArgumentException("O registro deve pertencer ao restaurante da fila.");
        }
        if (buscarPedido(registro.clienteDaFila(), registro.horarioDesejado()) != null) {
            throw new IllegalArgumentException("O cliente já está na fila para este horário.");
        }
        registros.add(registro);
    }

    public RegistroFila buscarPedido(Cliente cliente, LocalDateTime horario) {
        for (RegistroFila registro : registros) {
            Cliente cadastrado = registro.clienteDaFila();
            boolean mesmoCliente = cadastrado == cliente || (cliente != null && cliente.getId() != null
                    && cliente.getId().equals(cadastrado.getId()));
            if (mesmoCliente && registro.horarioDesejado().equals(horario)) {
                return registro;
            }
        }
        return null;
    }

    public int consultarPosicao(RegistroFila registro) {
        int indice = registros.indexOf(registro);
        LocalDateTime agora = LocalDateTime.now();
        if (indice < 0 || !registro.horarioDesejado().isAfter(agora)) {
            return -1;
        }
        int posicao = 0;
        for (int i = 0; i <= indice; i++) {
            RegistroFila anterior = registros.get(i);
            if (anterior.horarioDesejado().isAfter(agora)
                    && Reserva.horariosSobrepostos(anterior.horarioDesejado(), registro.horarioDesejado())) {
                posicao++;
            }
        }
        return posicao;
    }

    public boolean remover(RegistroFila registro) {
        return registros.remove(registro);
    }

    public List<RegistroFila> getRegistros() {
        return Collections.unmodifiableList(registros);
    }

    public List<RegistroFila> candidatosPara(Mesa mesa, LocalDateTime horario) {
        List<RegistroFila> candidatos = new ArrayList<>();
        for (RegistroFila registro : registros) {
            if (registro.podeOcupar(mesa, horario)) {
                candidatos.add(registro);
            }
        }
        return candidatos;
    }

    private boolean mesmoRestaurante(Restaurante outro) {
        return restaurante == outro || (restaurante.getId() != null && outro != null
                && restaurante.getId().equals(outro.getId()));
    }
}
