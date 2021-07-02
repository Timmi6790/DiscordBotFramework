package de.timmi6790.discord_framework.module.modules.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

/**
 * Database module.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Log4j2
public class DatabaseModule extends AbstractModule {
    private static final String TEST_QUERY = "SELECT 1;";

    /**
     * Database access point
     */
    private Jdbi jdbi;

    /**
     * Instantiates a new Database module.
     */
    public DatabaseModule() {
        super("Database");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class
        );

        this.addLoadAfterDependencies(
                MetricModule.class
        );
    }

    /**
     * Check if the connection to the database is valid.
     *
     * @return true if the connection is valid
     */
    private boolean isConnectedToDatabase() {
        try {
            return this.jdbi.withHandle(handle -> {
                handle
                        .createUpdate(TEST_QUERY)
                        .execute();
                return Boolean.TRUE;
            });
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Runs the database versioning control
     *
     * @param url      the url
     * @param user     the user
     * @param password the password
     */
    private void databaseVersioning(final String url, final String user, final String password) {
        Flyway.configure()
                .dataSource(url, user, password)
                .load()
                .migrate();
    }

    private HikariConfig getHikariConfig(final Config databaseConfig) {
        final HikariConfig hikariConfig = new HikariConfig();
        
        hikariConfig.setJdbcUrl(databaseConfig.getUrl());
        hikariConfig.setUsername(databaseConfig.getName());
        hikariConfig.setPassword(databaseConfig.getPassword());

        hikariConfig.addDataSourceProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        // I'm not sure if those properties even work for postgres
        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hikariConfig.addDataSourceProperty("useServerPrepStmts", true);

        return hikariConfig;
    }

    @Override
    public boolean onInitialize() {
        final Config databaseConfig = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());

        final HikariConfig hikariConfig = this.getHikariConfig(databaseConfig);

        // Metrics
        this.getModule(MetricModule.class).ifPresent(metric ->
                hikariConfig.setMetricRegistry(metric.getMeterRegistry())
        );

        this.jdbi = Jdbi.create(new HikariDataSource(hikariConfig));
        // Check if the connection is valid before doing any further actions
        if (!this.isConnectedToDatabase()) {
            log.error("Invalid database credentials");
            return false;
        }

        this.databaseVersioning(databaseConfig.getUrl(), databaseConfig.getName(), databaseConfig.getPassword());
        return true;
    }
}
