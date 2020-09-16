package de.timmi6790.discord_framework.fake_modules;

import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.jdbi.v3.core.Jdbi;
import org.testcontainers.containers.MariaDBContainer;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FakeDatabaseModel extends DatabaseModule {
    private final Jdbi jdbi;

    public FakeDatabaseModel(final MariaDBContainer mariaDBContainer) {
        this.jdbi = Jdbi.create(mariaDBContainer.getJdbcUrl(), mariaDBContainer.getUsername(), mariaDBContainer.getPassword());
    }

    @Override
    public Jdbi getJdbi() {
        return this.jdbi;
    }
}
