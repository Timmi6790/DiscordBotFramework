package de.timmi6790.discord_framework;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {
    @Container
    public static final MariaDBContainer MARIA_DB_CONTAINER = new MariaDBContainer();

    static {
        MARIA_DB_CONTAINER.start();

        // Good way to assure that the version files are working
        final Flyway flyway = Flyway.configure()
                .dataSource(MARIA_DB_CONTAINER.getJdbcUrl(), MARIA_DB_CONTAINER.getUsername(), MARIA_DB_CONTAINER.getPassword())
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }
}