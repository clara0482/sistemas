import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.Session;

public class Repositorio {
    public Restaurante buscarRestaurante(Session session, String nome) {
        return session.createQuery("from Restaurante where nome = :nome", Restaurante.class)
                .setParameter("nome", nome).setMaxResults(1).uniqueResult();
    }

    public List<Restaurante> listarRestaurantes(Session session) {
        return session.createQuery("from Restaurante order by nome", Restaurante.class).getResultList();
    }

    public Cliente buscarClientePorTelefone(Session session, String telefone) {
        List<Cliente> encontrados = session.createQuery("from Cliente c where "
                        + "replace(replace(replace(replace(replace(c.telefone, '(', ''), ')', ''), "
                        + "'-', ''), ' ', ''), '.', '') = :telefone",
                        Cliente.class)
                .setParameter("telefone", telefone)
                .setMaxResults(2).getResultList();
        if (encontrados.size() > 1) {
            throw new IllegalStateException("Mais de um cliente cadastrado com este telefone.");
        }
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    public boolean mesaOcupada(Session session, long mesaId, LocalDateTime horario) {
        String consulta = "select count(r) from Reserva r where r.mesa.id = :mesa "
                + "and r.horario > :inicio and r.horario < :fim and r.status = :status";
        long quantidade = session.createQuery(consulta, Long.class)
                .setParameter("mesa", mesaId)
                .setParameter("inicio", horario.minusHours(Reserva.DURACAO_HORAS))
                .setParameter("fim", horario.plusHours(Reserva.DURACAO_HORAS))
                .setParameter("status", StatusReserva.ATIVA)
                .getSingleResult();
        return quantidade > 0;
    }

    public Reserva buscarReservaAtivaDoCliente(Session session, long clienteId, LocalDateTime horario) {
        return session.createQuery("select r from Reserva r join fetch r.mesa m "
                        + "where r.cliente.id = :cliente "
                        + "and r.horario > :inicio and r.horario < :fim and r.status = :status order by r.id",
                        Reserva.class)
                .setParameter("cliente", clienteId)
                .setParameter("inicio", horario.minusHours(Reserva.DURACAO_HORAS))
                .setParameter("fim", horario.plusHours(Reserva.DURACAO_HORAS))
                .setParameter("status", StatusReserva.ATIVA)
                .setMaxResults(1).uniqueResult();
    }

    public List<Reserva> listarReservas(Session session, long clienteId) {
        return session.createQuery("select r from Reserva r join fetch r.mesa m "
                        + "join fetch m.restaurante where r.cliente.id = :cliente order by r.horario desc", Reserva.class)
                .setParameter("cliente", clienteId).getResultList();
    }

    public RegistroFila buscarPedidoConflitante(Session session, long clienteId, long restauranteId,
            LocalDateTime horario) {
        return session.createQuery("from RegistroFila f where f.cliente.id = :cliente "
                        + "and f.horarioDesejado > :agora "
                        + "and f.horarioDesejado > :inicio and f.horarioDesejado < :fim "
                        + "and (f.restaurante.id <> :restaurante or f.horarioDesejado <> :horario) order by f.id",
                        RegistroFila.class)
                .setParameter("cliente", clienteId)
                .setParameter("restaurante", restauranteId)
                .setParameter("horario", horario)
                .setParameter("agora", LocalDateTime.now())
                .setParameter("inicio", horario.minusHours(Reserva.DURACAO_HORAS))
                .setParameter("fim", horario.plusHours(Reserva.DURACAO_HORAS))
                .setMaxResults(1).uniqueResult();
    }

    public void removerPedidosVencidos(Session session) {
        session.createMutationQuery("delete from RegistroFila where horarioDesejado <= :agora")
                .setParameter("agora", LocalDateTime.now()).executeUpdate();
    }

    public List<RegistroFila> listarFila(Session session, long restauranteId) {
        return session.createQuery("select f from RegistroFila f join fetch f.cliente "
                        + "where f.restaurante.id = :restaurante and f.horarioDesejado > :agora order by f.id",
                        RegistroFila.class)
                .setParameter("restaurante", restauranteId)
                .setParameter("agora", LocalDateTime.now()).getResultList();
    }

    public List<RegistroFila> listarFilaCompleta(Session session) {
        return session.createQuery("select f from RegistroFila f join fetch f.cliente "
                        + "join fetch f.restaurante where f.horarioDesejado > :agora "
                        + "order by f.restaurante.id, f.id", RegistroFila.class)
                .setParameter("agora", LocalDateTime.now()).getResultList();
    }
}
