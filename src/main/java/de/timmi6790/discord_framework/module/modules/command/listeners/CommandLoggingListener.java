package de.timmi6790.discord_framework.module.modules.command.listeners;

import de.timmi6790.discord_framework.module.modules.command.events.CommandExecutionEvent;
import de.timmi6790.discord_framework.module.modules.event.EventPriority;
import de.timmi6790.discord_framework.module.modules.event.SubscribeEvent;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommandLoggingListener {
    @SubscribeEvent(priority = EventPriority.LOW, ignoreCanceled = true)
    public void afterCommandExecution(final CommandExecutionEvent.Post event) {
        // TODO: Add logging in prometheus
    }
}
