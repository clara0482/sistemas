import java.util.ArrayList;

public class FilaEspera {
    private ArrayList<RegistroFila> registros;

    public FilaEspera() {
        this.registros = new ArrayList<>();
    }

    // retorna a posição do cliente na fila daquele horário
    public int entrarFila(Cliente cliente, int quantidadePessoas, String horario) {
        RegistroFila novoRegistro = new RegistroFila(cliente, quantidadePessoas, horario);
        registros.add(novoRegistro);
        return acompanharPosicao(cliente);
    }

    public boolean contem(Cliente cliente) {
        return buscar(cliente) != null;
    }

    public RegistroFila buscar(Cliente cliente) {
        for (RegistroFila r : registros) {
            if (r.getCliente().equals(cliente)) {
                return r;
            }
        }
        return null;
    }

    // regra de negocio: a posição é contada apenas entre quem espera
    // pelo MESMO horario (quem espera as 22:00 não atrasa quem espera as 19:00).
    // apenas consulta
    public int acompanharPosicao(Cliente cliente) {
        RegistroFila alvo = buscar(cliente);
        if (alvo == null) return -1;

        int posicao = 0;
        for (RegistroFila r : registros) {
            if (r.getHorario().equals(alvo.getHorario())) {
                posicao++;
                if (r == alvo) return posicao;
            }
        }
        return -1;
    }

    public RegistroFila chamarProximo() {
        if (!registros.isEmpty()) {
            return registros.remove(0);
        }
        return null;
    }
}