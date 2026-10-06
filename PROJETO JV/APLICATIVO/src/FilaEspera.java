import java.util.ArrayList;

public class FilaEspera {
    private ArrayList<RegistroFila> registros; // Coleção 1

    public FilaEspera() {
        this.registros = new ArrayList<>();
    }

    public void entrarFila(Cliente cliente, int quantidadePessoas) {
        RegistroFila novoRegistro = new RegistroFila(cliente, quantidadePessoas);
        registros.add(novoRegistro);
        System.out.println(cliente.getNome() + " entrou na fila na posição " + registros.size());
    }

    // Regra de Negócio: Calcular posição na fila e definir prioridade
    public int acompanharPosicao(Cliente cliente) {
        for (int i = 0; i < registros.size(); i++) {
            if (registros.get(i).getCliente().equals(cliente)) {
                // Se for o próximo (posição 0 ou 1) e não foi avisado, manda o aviso
                if (i <= 1 && !registros.get(i).isAvisoPrevioEnviado()) {
                    registros.get(i).enviarAviso();
                }
                return i + 1; 
            }
        }
        return -1; // Não está na fila
    }

    public RegistroFila chamarProximo() {
        if (!registros.isEmpty()) {
            return registros.remove(0);
        }
        return null;
    }
}