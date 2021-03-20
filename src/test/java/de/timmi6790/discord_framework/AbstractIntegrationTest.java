package de.timmi6790.discord_framework;

import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.database.Config;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.doReturn;

@Testcontainers
public abstract class AbstractIntegrationTest {
    @Container
    private static final MariaDBContainer MARIA_DB_CONTAINER = new MariaDBContainer();

    @Spy
    public static final DatabaseModule databaseModule = Mockito.spy(DatabaseModule.class);

    static {
        MARIA_DB_CONTAINER.start();

        final ConfigModule configModule = Mockito.spy(new ConfigModule());
        // doReturn(configModule).when(databaseModule).getModuleOrThrow(ConfigModule.class);

        final Config databaseConfig = new Config();
        databaseConfig.setUrl(MARIA_DB_CONTAINER.getJdbcUrl());
        databaseConfig.setName(MARIA_DB_CONTAINER.getUsername());
        databaseConfig.setPassword(MARIA_DB_CONTAINER.getPassword());

        doReturn(databaseConfig).when(configModule).registerAndGetConfig(databaseModule, new Config());
        // databaseModule.onInitialize();
    }
}