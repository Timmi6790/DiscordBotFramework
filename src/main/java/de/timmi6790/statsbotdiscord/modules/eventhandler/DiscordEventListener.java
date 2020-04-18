package de.timmi6790.statsbotdiscord.modules.eventhandler;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.events.EventMessageReceived;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class DiscordEventListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@Nonnull final MessageReceivedEvent event) {
        final EventMessageReceived eventMessageReceived = new EventMessageReceived(event.getResponseNumber(), event.getMessage());
        StatsBot.getEventManager().callEvent(eventMessageReceived);
    }

    @Override
    public void onMessageReactionAdd(@Nonnull final MessageReactionAddEvent event) {
        StatsBot.getEventManager().callEvent(event);
    }

    @Override
    public void onMessageReactionRemove(@Nonnull final MessageReactionRemoveEvent event) {
        StatsBot.getEventManager().callEvent(event);
    }

    @Override
    public void onMessageDelete(@Nonnull final MessageDeleteEvent event) {
        StatsBot.getEventManager().callEvent(event);
    }
}
