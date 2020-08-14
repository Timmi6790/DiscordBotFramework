package de.timmi6790.discord_framework.modules.event;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.event.events.MessageReceivedIntEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class DiscordEventListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@Nonnull final net.dv8tion.jda.api.events.message.MessageReceivedEvent event) {
        final MessageReceivedIntEvent messageReceivedIntEvent = new MessageReceivedIntEvent(event.getResponseNumber(), event.getMessage());
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(EventModule.class).executeEvent(messageReceivedIntEvent);
    }

    @Override
    public void onMessageReactionAdd(@Nonnull final MessageReactionAddEvent event) {
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(EventModule.class).executeEvent(event);
    }

    @Override
    public void onMessageReactionRemove(@Nonnull final MessageReactionRemoveEvent event) {
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(EventModule.class).executeEvent(event);
    }

    @Override
    public void onMessageDelete(@Nonnull final MessageDeleteEvent event) {
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(EventModule.class).executeEvent(event);
    }
}
