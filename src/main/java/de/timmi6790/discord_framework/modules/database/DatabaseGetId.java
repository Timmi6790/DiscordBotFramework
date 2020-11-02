package de.timmi6790.discord_framework.modules.database;

import de.timmi6790.discord_framework.DiscordBot;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Database utility class to retrieve the id of an object.
 */
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class DatabaseGetId {
    @NonNull
    private final String sqlGetId;
    @NonNull
    private final String sqlInsertId;
    @NonNull
    private final Supplier<Jdbi> databaseSupplier;

    /**
     * Instantiates a new database get id.
     *
     * @param sqlGetId    the sql get id code
     * @param sqlInsertId the sql insert id code
     */
    protected DatabaseGetId(final String sqlGetId, final String sqlInsertId) {
        this.sqlGetId = sqlGetId;
        this.sqlInsertId = sqlInsertId;
        this.databaseSupplier = () -> DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi();
    }

    /**
     * Retrieve the database id if present or create it
     *
     * @return the database id
     */
    protected int retrieveDatabaseId() {
        return this.databaseSupplier.get().withHandle(handle ->
                handle.createQuery(this.sqlGetId)
                        .bindMap(this.getGetIdParameters())
                        .mapTo(int.class)
                        .findFirst()
                        .orElseGet(() -> {
                            handle.createUpdate(this.sqlInsertId)
                                    .bindMap(this.getInsertIdParameters())
                                    .execute();

                            return handle.createQuery(this.sqlGetId)
                                    .bindMap(this.getGetIdParameters())
                                    .mapTo(int.class)
                                    .first();
                        })
        );
    }

    /**
     * Get the parameter mappings for the get id request
     *
     * @return parameters [Key, Value]
     */
    protected abstract @NonNull Map<String, Object> getGetIdParameters();

    /**
     * Get the parameter mappings for the id insert
     *
     * @return parameters [Key, Value]
     */
    protected abstract @NonNull Map<String, Object> getInsertIdParameters();
}
