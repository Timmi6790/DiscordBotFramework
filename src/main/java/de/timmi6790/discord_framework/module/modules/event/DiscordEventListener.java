package de.timmi6790.discord_framework.module.modules.event;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class DiscordEventListener implements EventListener {
    private final EventModule eventModule;

    @Override
    public void onEvent(@Nonnull final GenericEvent event) {
        this.eventModule.executeEvent(event);
    }
}

