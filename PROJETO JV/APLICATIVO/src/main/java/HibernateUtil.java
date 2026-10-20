import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {
    private static final Logger logger = Logger.getLogger("org.hibernate");
    private static SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    private static SessionFactory criarSessionFactory() {
        try {
            Files.createDirectories(Path.of("dados"));
            logger.setLevel(Level.SEVERE);
            Configuration config = new Configuration();
            config.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            config.setProperty("hibernate.connection.url", "jdbc:h2:file:./dados/reservas");
            config.setProperty("hibernate.connection.username", "sa");
            config.setProperty("hibernate.connection.password", "");
            config.setProperty("hibernate.hbm2ddl.auto", "update");
            config.setProperty("hibernate.show_sql", "false");
            config.addAnnotatedClass(Cliente.class);
            config.addAnnotatedClass(Restaurante.class);
            config.addAnnotatedClass(Mesa.class);
            config.addAnnotatedClass(Reserva.class);
            config.addAnnotatedClass(RegistroFila.class);
            return config.buildSessionFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível abrir o banco H2 em dados/reservas. "
                    + "Feche outra execução do aplicativo e confira a permissão de escrita na pasta dados.", e);
        }
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = criarSessionFactory();
        }
        return sessionFactory;
    }

    public static synchronized void fechar() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
