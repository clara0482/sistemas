import java.nio.file.Path;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

final class BancoTeste {
    private BancoTeste() {
    }

    static SessionFactory abrir(Path arquivo) {
        return new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:file:" + arquivo.toAbsolutePath().toString().replace('\\', '/'))
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.hbm2ddl.auto", "update")
                .setProperty("hibernate.show_sql", "false")
                .addAnnotatedClass(Cliente.class)
                .addAnnotatedClass(Restaurante.class)
                .addAnnotatedClass(Mesa.class)
                .addAnnotatedClass(Reserva.class)
                .addAnnotatedClass(RegistroFila.class)
                .buildSessionFactory();
    }
}