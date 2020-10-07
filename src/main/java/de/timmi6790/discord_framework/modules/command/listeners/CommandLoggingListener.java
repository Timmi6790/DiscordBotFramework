package de.timmi6790.discord_framework.modules.command.listeners;

import de.timmi6790.discord_framework.modules.command.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.modules.event.EventPriority;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;

@AllArgsConstructor
public class CommandLoggingListener {
    private static final String INSERT_COMMAND_LOG = "INSERT INTO command_log(command_id, command_cause_id, command_status_id, in_guild) VALUES(:commandId, " +
            "(SELECT id FROM command_cause WHERE cause_name = :causeName LIMIT 1), (SELECT id FROM command_status WHERE status_name = :statusName LIMIT 1), :inGuild);";

    private final Jdbi database;

    @SubscribeEvent(priority = EventPriority.LOW, ignoreCanceled = true)
    public void afterCommandExecution(final CommandExecutionEvent.Post event) {
        this.database.useHandle(handle ->
                handle.createUpdate(INSERT_COMMAND_LOG)
                        .bind("commandId", event.getCommand().getDbId())
                        .bind("causeName", event.getParameters().getCommandCause().name().toLowerCase())
                        .bind("statusName", event.getCommandResult().name().toLowerCase())
                        .bind("inGuild", event.getParameters().isGuildCommand())
                        .execute()
        );
    }
}
