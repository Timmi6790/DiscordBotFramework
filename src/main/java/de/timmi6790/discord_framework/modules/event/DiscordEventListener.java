package de.timmi6790.discord_framework.modules.event;

import de.timmi6790.discord_framework.modules.GetModule;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;

public class DiscordEventListener extends GetModule<EventModule> implements EventListener {
    @Override
    public void onEvent(@Nonnull final GenericEvent event) {
        this.getModule().executeEvent(event);
    }
}

