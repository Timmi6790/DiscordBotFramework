package de.timmi6790.discord_framework;

import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.database.Config;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.doReturn;

@Testcontainers
public abstract class AbstractIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName
                    .parse("timescale/timescaledb:2.3.0-pg12")
                    .asCompatibleSubstituteFor("postgres")
    );

    @Spy
    public static final DatabaseModule databaseModule = Mockito.spy(new DatabaseModule());

    static {
        POSTGRES_SQL_CONTAINER.start();

        final ConfigModule configModule = Mockito.spy(new ConfigModule());
        doReturn(configModule).when(databaseModule).getModuleOrThrow(ConfigModule.class);

        final Config databaseConfig = new Config();
        databaseConfig.setUrl(POSTGRES_SQL_CONTAINER.getJdbcUrl());
        databaseConfig.setName(POSTGRES_SQL_CONTAINER.getUsername());
        databaseConfig.setPassword(POSTGRES_SQL_CONTAINER.getPassword());
        
        doReturn(databaseConfig).when(configModule).registerAndGetConfig(databaseModule, new Config());
        databaseModule.onInitialize();
    }
}