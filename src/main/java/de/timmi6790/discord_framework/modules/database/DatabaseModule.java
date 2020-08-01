package de.timmi6790.discord_framework.modules.database;

import de.timmi6790.discord_framework.modules.AbstractModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;

@EqualsAndHashCode(callSuper = true)

public class DatabaseModule extends AbstractModule {
    @Getter
    private final Jdbi jdbi;

    public DatabaseModule(final String url, final String dbName, final String password) {
        super("Database");

        this.jdbi = Jdbi.create(url, dbName, password);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
