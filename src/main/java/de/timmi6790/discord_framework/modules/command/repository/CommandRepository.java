package de.timmi6790.discord_framework.modules.command.repository;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandCause;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import lombok.NonNull;

public interface CommandRepository {
    void init(CommandCause[] causes, CommandResult[] results);

    int getCommandDatabaseId(@NonNull final AbstractCommand command);

    void logCommandExecution(int databaseId, @NonNull CommandCause cause, @NonNull CommandResult result, boolean inGuild);
}
