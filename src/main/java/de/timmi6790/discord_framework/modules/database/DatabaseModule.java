package de.timmi6790.discord_framework.modules.database;


import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;

/**
 * Database module.
 */
@EqualsAndHashCode
@Getter
@Log4j2
public class DatabaseModule implements Module {
    private static final String TEST_QUERY = "SELECT 1;";

    /**
     * Database access point
     */
    private final Jdbi jdbi;

    /**
     * Instantiates a new Database module.
     */
    public DatabaseModule(final ConfigModule configModule) {
        final Config databaseConfig = configModule
                .registerAndGetConfig(this, new Config());

        this.jdbi = Jdbi.create(databaseConfig.getUrl(), databaseConfig.getName(), databaseConfig.getPassword());
        // Check if the connection is valid before doing any futher actions
        if (!this.isConnectedToDatabase()) {
            log.error("Invalid database credentials");
            // return false;
            return;
        }

        this.executeDatabaseVersioning(
                databaseConfig.getUrl(),
                databaseConfig.getName(),
                databaseConfig.getPassword()
        );
    }

    @Override
    public String getName() {
        return "Database";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getAuthors() {
        return new String[]{"Timmi6790"};
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
    private void executeDatabaseVersioning(final String url, final String user, final String password) {
        final Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }

    /**
     * Retrieve or create id.
     *
     * @param sqlSelectIdQuery the sql query to select the id
     * @param getParameters    the parameters for the sql select query
     * @param sqlInsertIdQuery the sql query to insert the id into the repository
     * @param insertParameters the parameters for the sql insert query
     * @return the repository id
     */
    public int retrieveOrCreateId(final String sqlSelectIdQuery,
                                  final Map<String, ?> getParameters,
                                  final String sqlInsertIdQuery,
                                  final Map<String, ?> insertParameters) {
        return this.jdbi.withHandle(handle ->
                handle.createQuery(sqlSelectIdQuery)
                        .bindMap(getParameters)
                        .mapTo(int.class)
                        .findFirst()
                        .orElseGet(() -> {
                            handle.createUpdate(sqlInsertIdQuery)
                                    .bindMap(insertParameters)
                                    .execute();

                            return handle.createQuery(sqlSelectIdQuery)
                                    .bindMap(getParameters)
                                    .mapTo(int.class)
                                    .first();
                        })
        );
    }
}
