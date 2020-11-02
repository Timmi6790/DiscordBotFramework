package de.timmi6790.discord_framework.modules.command.listeners;

import de.timmi6790.discord_framework.modules.command.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.modules.command.repository.CommandRepository;
import de.timmi6790.discord_framework.modules.event.EventPriority;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommandLoggingListener {
    private final CommandRepository commandRepository;

    @SubscribeEvent(priority = EventPriority.LOW, ignoreCanceled = true)
    public void afterCommandExecution(final CommandExecutionEvent.Post event) {
        this.commandRepository.logCommandExecution(
                event.getCommand().getDbId(),
                event.getParameters().getCommandCause(),
                event.getCommandResult(),
                event.getParameters().isGuildCommand()
        );
    }
}
