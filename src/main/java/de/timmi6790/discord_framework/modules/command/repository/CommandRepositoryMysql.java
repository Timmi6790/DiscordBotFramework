package de.timmi6790.discord_framework.modules.command.repository;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandCause;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;

import java.util.Arrays;

public class CommandRepositoryMysql implements CommandRepository {
    private static final String COMMAND_NAME = "commandName";
    private static final String CAUSE_NAME = "causeName";
    private static final String STATUS_NAME = "statusName";

    private static final String INSERT_COMMAND_LOG = "INSERT INTO command_log(command_id, command_cause_id, command_status_id, in_guild) VALUES(:commandId, " +
            "(SELECT id FROM command_cause WHERE cause_name = :causeName LIMIT 1), (SELECT id FROM command_status WHERE status_name = :statusName LIMIT 1), :inGuild);";

    private static final String GET_COMMAND_ID = "SELECT id FROM `command` WHERE command_name = :commandName LIMIT 1;";
    private static final String INSERT_NEW_COMMAND = "INSERT INTO command(command_name) VALUES(:commandName);";

    private static final String GET_COMMAND_CAUSE_COUNT = "SELECT COUNT(*) FROM `command_cause` WHERE cause_name = :causeName LIMIT 1;";
    private static final String INSERT_COMMAND_CAUSE = "INSERT INTO command_cause(cause_name) VALUES(:causeName);";

    private static final String GET_COMMAND_STATUS_COUNT = "SELECT COUNT(*) FROM `command_status` WHERE status_name = :statusName LIMIT 1;";
    private static final String INSERT_COMMAND_STATUS = "INSERT INTO command_status(status_name) VALUES(:statusName);";

    private final Jdbi database;

    public CommandRepositoryMysql(final CommandModule module) {
        this.database = module.getModuleOrThrow(DatabaseModule.class).getJdbi();
    }

    @Override
    public void init(final CommandCause[] causes, final CommandResult[] results) {
        // CommandCause
        this.database.useHandle(handle ->
                Arrays.stream(causes)
                        .parallel()
                        .map(commandCause -> commandCause.name().toLowerCase())
                        .filter(nameLower ->
                                handle.createQuery(GET_COMMAND_CAUSE_COUNT)
                                        .bind(CAUSE_NAME, nameLower)
                                        .mapTo(int.class)
                                        .first() == 0
                        )
                        .forEach(nameLower ->
                                handle.createUpdate(INSERT_COMMAND_CAUSE)
                                        .bind(CAUSE_NAME, nameLower)
                                        .execute()
                        )
        );

        // CommandStatus
        this.database.useHandle(handle ->
                Arrays.stream(results)
                        .parallel()
                        .map(commandResult -> commandResult.name().toLowerCase())
                        .filter(nameLower ->
                                handle.createQuery(GET_COMMAND_STATUS_COUNT)
                                        .bind(STATUS_NAME, nameLower)
                                        .mapTo(int.class)
                                        .first() == 0
                        )
                        .forEach(nameLower ->
                                handle.createUpdate(INSERT_COMMAND_STATUS)
                                        .bind(STATUS_NAME, nameLower)
                                        .execute()
                        )
        );
    }

    @Override
    public int getCommandDatabaseId(@NonNull final AbstractCommand command) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_COMMAND_ID)
                        .bind(COMMAND_NAME, command.getName())
                        .mapTo(int.class)
                        .findFirst()
                        .orElseGet(() -> {
                            handle.createUpdate(INSERT_NEW_COMMAND)
                                    .bind(COMMAND_NAME, command.getName())
                                    .execute();

                            return handle.createQuery(GET_COMMAND_ID)
                                    .bind(COMMAND_NAME, command.getName())
                                    .mapTo(int.class)
                                    .first();
                        })
        );
    }

    @Override
    public void logCommandExecution(final int databaseId,
                                    @NonNull final CommandCause cause,
                                    @NonNull final CommandResult result,
                                    final boolean inGuild) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_COMMAND_LOG)
                        .bind("commandId", databaseId)
                        .bind(CAUSE_NAME, cause.name().toLowerCase())
                        .bind(STATUS_NAME, result.name().toLowerCase())
                        .bind("inGuild", inGuild)
                        .execute()
        );
    }
}
