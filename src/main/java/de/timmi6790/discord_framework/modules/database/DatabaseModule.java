package de.timmi6790.discord_framework.modules.database;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

@EqualsAndHashCode(callSuper = true)
public class DatabaseModule extends AbstractModule {
    @Getter
    private Jdbi jdbi;

    public DatabaseModule() {
        super("Database");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class
        );
    }

    private void databaseMigration(final String url, final String user, final String password) {
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
        this.databaseMigration(databaseConfig.getUrl(), databaseConfig.getName(), databaseConfig.getPassword());
    }
}
