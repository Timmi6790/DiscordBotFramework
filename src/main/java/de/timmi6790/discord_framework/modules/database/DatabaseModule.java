package de.timmi6790.discord_framework.modules.database;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;

/**
 * Database module.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class DatabaseModule extends AbstractModule {
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
    }

    /**
     * Runs the database versioning control
     *
     * @param url      the url
     * @param user     the user
     * @param password the password
     */
    private void databaseVersioning(final String url, final String user, final String password) {
        final Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }

    @Override
    public void onInitialize() {
        final Config databaseConfig = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());
        this.jdbi = Jdbi.create(databaseConfig.getUrl(), databaseConfig.getName(), databaseConfig.getPassword());
        this.databaseVersioning(databaseConfig.getUrl(), databaseConfig.getName(), databaseConfig.getPassword());
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
