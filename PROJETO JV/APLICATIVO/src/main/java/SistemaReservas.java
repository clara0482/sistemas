import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.LockModeType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class SistemaReservas {
    public static final List<LocalTime> HORARIOS = List.of(
            LocalTime.of(18, 0), LocalTime.of(19, 0), LocalTime.of(19, 30),
            LocalTime.of(20, 0), LocalTime.of(20, 30), LocalTime.of(21, 0), LocalTime.of(22, 0));

    private final Repositorio repositorio = new Repositorio();
    private final SessionFactory fabrica;
    private static final Object CADASTRO_CLIENTES = new Object();

    public SistemaReservas() {
        this(HibernateUtil.getSessionFactory());
    }

    SistemaReservas(SessionFactory fabrica) {
        if (fabrica == null) {
            throw new IllegalArgumentException("Informe a conexão com o banco.");
        }
        this.fabrica = fabrica;
    }

    public void cadastrarRestaurantesIniciais() {
        try (Session session = fabrica.openSession()) {
            Transaction transacao = session.beginTransaction();
            try {
                cadastrarRestauranteSeNecessario(session, "Sabor POO (Massas e Vinhos)");
                cadastrarRestauranteSeNecessario(session, "Java Grill (Churrascaria)");
                cadastrarRestauranteSeNecessario(session, "Sushi Byte (Comida Japonesa)");
                repositorio.removerPedidosVencidos(session);
                transacao.commit();
            } catch (RuntimeException e) {
                transacao.rollback();
                throw e;
            }
        }
    }

    private void cadastrarRestauranteSeNecessario(Session session, String nome) {
        Restaurante restaurante = repositorio.buscarRestaurante(session, nome);
        if (restaurante == null) {
            restaurante = new Restaurante(nome);
            session.persist(restaurante);
        }
        int[] capacidades = {2, 4, 4, 6, 8};
        for (int i = 0; i < capacidades.length; i++) {
            if (restaurante.buscarMesa(i + 1) == null) {
                session.persist(restaurante.adicionarMesa(i + 1, capacidades[i]));
            }
        }
    }

    public Cliente cadastrarCliente(String nome, String telefone) {
        Cliente dados = new Cliente(nome, telefone);
        synchronized (CADASTRO_CLIENTES) {
            return salvarCliente(dados);
        }
    }

    private Cliente salvarCliente(Cliente dados) {
        try (Session session = fabrica.openSession()) {
            Transaction transacao = session.beginTransaction();
            try {
                Cliente cliente = repositorio.buscarClientePorTelefone(session, dados.getTelefone());
                if (cliente == null) {
                    cliente = dados;
                    session.persist(cliente);
                } else {
                    if (!cliente.getNome().equalsIgnoreCase(dados.getNome())) {
                        throw new IllegalArgumentException("Este telefone pertence a outro cadastro. Confira o nome e o telefone.");
                    }
                    System.out.println("Este telefone já possui cadastro. Os dados existentes foram mantidos.");
                }
                transacao.commit();
                return cliente;
            } catch (RuntimeException e) {
                transacao.rollback();
                throw e;
            }
        }
    }

    public List<Restaurante> listarRestaurantes() {
        try (Session session = fabrica.openSession()) {
            return repositorio.listarRestaurantes(session);
        }
    }

    public Map<Mesa, Boolean> listarMesas(long restauranteId, LocalDateTime horario) {
        if (horario == null) {
            throw new IllegalArgumentException("Informe o horário.");
        }
        try (Session session = fabrica.openSession()) {
            Restaurante restaurante = session.find(Restaurante.class, restauranteId);
            if (restaurante == null) {
                throw new IllegalArgumentException("Restaurante não encontrado.");
            }
            Map<Mesa, Boolean> resultado = new LinkedHashMap<>();
            for (Mesa mesa : restaurante.getMesas()) {
                resultado.put(mesa, !repositorio.mesaOcupada(session, mesa.getId(), horario));
            }
            return resultado;
        }
    }

    private FilaEspera carregarFila(Session session, Restaurante restaurante) {
        return new FilaEspera(restaurante, repositorio.listarFila(session, restaurante.getId()));
    }

    private RegistroFila proximoDaFila(Session session, FilaEspera fila, Mesa mesa, LocalDateTime horario) {
        for (RegistroFila registro : fila.candidatosPara(mesa, horario)) {
            if (repositorio.mesaOcupada(session, mesa.getId(), registro.horarioDesejado())) {
                continue;
            }
            Cliente cliente = registro.clienteDaFila();
            session.find(Cliente.class, cliente.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (repositorio.buscarReservaAtivaDoCliente(session, cliente.getId(), registro.horarioDesejado()) == null) {
                return registro;
            }
        }
        return null;
    }

    private Reserva criarReserva(Session session, Cliente cliente, Mesa mesa, LocalDateTime horario, int pessoas) {
        if (repositorio.buscarReservaAtivaDoCliente(session, cliente.getId(), horario) != null) {
            throw new IllegalArgumentException("Você já possui uma reserva nesse período.");
        }
        if (repositorio.mesaOcupada(session, mesa.getId(), horario)) {
            throw new IllegalArgumentException("A mesa já está ocupada nesse período.");
        }
        Reserva reserva = new Reserva(cliente, mesa, horario, pessoas);
        session.persist(reserva);
        return reserva;
    }

    public Reserva solicitarMesa(long restauranteId, long clienteId, int pessoas, LocalDateTime horario) {
        if (pessoas < 1 || pessoas > 8) {
            throw new IllegalArgumentException("Informe de 1 a 8 pessoas.");
        }
        if (horario == null || !HORARIOS.contains(horario.toLocalTime()) || !horario.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Escolha um horário disponível em uma data futura.");
        }
        try (Session session = fabrica.openSession()) {
            Transaction transacao = session.beginTransaction();
            try {
                Restaurante restaurante = session.find(Restaurante.class, restauranteId, LockModeType.PESSIMISTIC_WRITE);
                Cliente cliente = session.find(Cliente.class, clienteId, LockModeType.PESSIMISTIC_WRITE);
                if (restaurante == null || cliente == null) {
                    throw new IllegalArgumentException("Cliente ou restaurante não encontrado.");
                }
                if (repositorio.buscarPedidoConflitante(session, clienteId, restauranteId, horario) != null) {
                    throw new IllegalArgumentException("Você já possui uma solicitação na fila nesse período. "
                            + "O pedido original foi mantido.");
                }
                Reserva reserva = buscarReservaExistente(session, restauranteId, clienteId, pessoas, horario);
                if (reserva == null) {
                    reserva = atenderPedido(session, restaurante, cliente, pessoas, horario);
                }
                transacao.commit();
                return reserva;
            } catch (RuntimeException e) {
                transacao.rollback();
                throw e;
            }
        }
    }

    private Reserva buscarReservaExistente(Session session, long restauranteId, long clienteId,
            int pessoas, LocalDateTime horario) {
        Reserva existente = repositorio.buscarReservaAtivaDoCliente(session, clienteId, horario);
        if (existente == null) {
            return null;
        }
        if (!existente.getHorario().equals(horario)
                || !existente.getMesa().getRestaurante().getId().equals(restauranteId)) {
            throw new IllegalArgumentException("Você já possui uma reserva nesse período.");
        }
        if (existente.getQuantidadePessoas() == null || existente.getQuantidadePessoas() != pessoas) {
            throw new IllegalArgumentException("A quantidade da reserva existente é diferente ou não foi informada. "
                    + "Use Alterar reserva para confirmar a quantidade.");
        }
        return existente;
    }

    private Reserva atenderPedido(Session session, Restaurante restaurante, Cliente cliente,
            int pessoas, LocalDateTime horario) {
        FilaEspera fila = carregarFila(session, restaurante);
        RegistroFila pedidoAnterior = fila.buscarPedido(cliente, horario);
        if (pedidoAnterior != null && pedidoAnterior.getQuantidadePessoas() != pessoas) {
            throw new IllegalArgumentException("Você já está na fila com outra quantidade de pessoas. "
                    + "O pedido original foi mantido.");
        }
        for (Mesa mesa : restaurante.getMesas()) {
            if (!mesa.comporta(pessoas) || repositorio.mesaOcupada(session, mesa.getId(), horario)) {
                continue;
            }
            RegistroFila primeiroNaFila = proximoDaFila(session, fila, mesa, horario);
            if (primeiroNaFila != null) {
                Reserva atendida = criarReserva(session, primeiroNaFila.clienteDaFila(), mesa,
                        primeiroNaFila.horarioDesejado(), primeiroNaFila.getQuantidadePessoas());
                removerDaFila(session, fila, primeiroNaFila);
                if (primeiroNaFila.clienteDaFila().getId().equals(cliente.getId())
                        && primeiroNaFila.horarioDesejado().equals(horario)) {
                    return atendida;
                }
                continue;
            }
            Reserva reserva = criarReserva(session, cliente, mesa, horario, pessoas);
            removerDaFila(session, fila, fila.buscarPedido(cliente, horario));
            return reserva;
        }
        if (pedidoAnterior == null) {
            RegistroFila registro = new RegistroFila(restaurante, cliente, pessoas, horario);
            fila.adicionar(registro);
            session.persist(registro);
        }
        return null;
    }

    private void removerDaFila(Session session, FilaEspera fila, RegistroFila registro) {
        if (registro != null && fila.remover(registro)) {
            session.remove(registro);
        }
    }

    public List<Reserva> listarReservas(long clienteId) {
        try (Session session = fabrica.openSession()) {
            return repositorio.listarReservas(session, clienteId);
        }
    }

    public boolean alterarReserva(long reservaId, long clienteId, int pessoas) {
        if (pessoas < 1 || pessoas > 8) {
            throw new IllegalArgumentException("Informe de 1 a 8 pessoas.");
        }
        try (Session session = fabrica.openSession()) {
            Transaction transacao = session.beginTransaction();
            try {
                Reserva reserva = session.get(Reserva.class, reservaId);
                if (reserva == null || !reserva.getCliente().getId().equals(clienteId)) {
                    throw new IllegalArgumentException("Reserva não encontrada para este cliente.");
                }
                Restaurante restaurante = session.find(Restaurante.class,
                        reserva.getMesa().getRestaurante().getId(), LockModeType.PESSIMISTIC_WRITE);
                session.find(Cliente.class, clienteId, LockModeType.PESSIMISTIC_WRITE);
                session.refresh(reserva);
                if (reserva.getStatus() != StatusReserva.ATIVA) {
                    throw new IllegalArgumentException("Só é possível alterar uma reserva ativa.");
                }
                if (!reserva.getHorario().isAfter(LocalDateTime.now())) {
                    throw new IllegalArgumentException("Não é possível alterar uma reserva cujo horário já começou.");
                }
                Mesa original = reserva.getMesa();
                Mesa escolhida = original.comporta(pessoas) ? original : null;
                if (escolhida == null) {
                    FilaEspera fila = carregarFila(session, restaurante);
                    for (Mesa mesa : restaurante.getMesas()) {
                        if (mesa.comporta(pessoas)
                                && !repositorio.mesaOcupada(session, mesa.getId(), reserva.getHorario())
                                && proximoDaFila(session, fila, mesa, reserva.getHorario()) == null) {
                            escolhida = mesa;
                            break;
                        }
                    }
                }
                if (escolhida == null) {
                    transacao.rollback();
                    return false;
                }
                reserva.alterar(escolhida, pessoas);
                if (!original.getId().equals(escolhida.getId())) {
                    session.flush();
                    atenderFilaDaMesa(session, restaurante, original, reserva.getHorario());
                }
                transacao.commit();
                return true;
            } catch (RuntimeException e) {
                transacao.rollback();
                throw e;
            }
        }
    }

    private void atenderFilaDaMesa(Session session, Restaurante restaurante, Mesa mesa, LocalDateTime horario) {
        FilaEspera fila = carregarFila(session, restaurante);
        RegistroFila proximo = proximoDaFila(session, fila, mesa, horario);
        if (proximo != null) {
            criarReserva(session, proximo.clienteDaFila(), mesa,
                    proximo.horarioDesejado(), proximo.getQuantidadePessoas());
            removerDaFila(session, fila, proximo);
        }
    }

    public boolean cancelarReserva(long reservaId, long clienteId) {
        try (Session session = fabrica.openSession()) {
            Transaction transacao = session.beginTransaction();
            try {
                Reserva reserva = session.get(Reserva.class, reservaId);
                if (reserva == null || !reserva.getCliente().getId().equals(clienteId)) {
                    transacao.rollback();
                    return false;
                }
                Restaurante restaurante = session.find(Restaurante.class,
                        reserva.getMesa().getRestaurante().getId(), LockModeType.PESSIMISTIC_WRITE);
                session.refresh(reserva);
                if (reserva.getStatus() != StatusReserva.ATIVA) {
                    transacao.rollback();
                    return false;
                }
                reserva.cancelar();
                session.flush();
                atenderFilaDaMesa(session, restaurante, reserva.getMesa(), reserva.getHorario());
                transacao.commit();
                return true;
            } catch (RuntimeException e) {
                transacao.rollback();
                throw e;
            }
        }
    }

    public Map<RegistroFila, Integer> consultarFila(long clienteId) {
        try (Session session = fabrica.openSession()) {
            Map<RegistroFila, Integer> resultado = new LinkedHashMap<>();
            FilaEspera fila = null;
            Long restauranteAnterior = null;
            for (RegistroFila registro : repositorio.listarFilaCompleta(session)) {
                Long restauranteId = registro.getRestaurante().getId();
                if (!restauranteId.equals(restauranteAnterior)) {
                    restauranteAnterior = restauranteId;
                    fila = new FilaEspera(registro.getRestaurante(), List.of());
                }
                fila.adicionar(registro);
                if (registro.clienteDaFila().getId().equals(clienteId)) {
                    int posicao = fila.consultarPosicao(registro);
                    if (posicao > 0) {
                        resultado.put(registro, posicao);
                    }
                }
            }
            return resultado;
        }
    }
}
