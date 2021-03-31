package de.timmi6790.discord_framework.module.modules.command.listeners;

import de.timmi6790.discord_framework.module.modules.command.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.module.modules.command.repository.CommandRepository;
import de.timmi6790.discord_framework.module.modules.event.EventPriority;
import de.timmi6790.discord_framework.module.modules.event.SubscribeEvent;
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
