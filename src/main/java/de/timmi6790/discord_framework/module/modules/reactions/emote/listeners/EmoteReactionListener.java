package de.timmi6790.discord_framework.module.modules.reactions.emote.listeners;

import de.timmi6790.discord_framework.module.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.module.modules.reactions.emote.EmoteReaction;
import de.timmi6790.discord_framework.module.modules.reactions.emote.EmoteReactionModule;
import de.timmi6790.discord_framework.module.modules.reactions.emote.actions.EmoteAction;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.Optional;

@RequiredArgsConstructor
public class EmoteReactionListener {
    private final EmoteReactionModule module;

    @SubscribeEvent
    public void onReactionAdd(final MessageReactionAddEvent event) {
        final Optional<EmoteReaction> emoteReactionOpt = this.module.getEmoteReaction(event.getMessageIdLong());
        if (emoteReactionOpt.isEmpty()) {
            return;
        }

        final EmoteReaction emoteReaction = emoteReactionOpt.get();
        if (!emoteReaction.getUsers().contains(event.getUserIdLong())) {
            return;
        }

        final EmoteAction emoteAction = emoteReaction.getEmotes().get(event.getReaction().getReactionEmote().getName());
        if (emoteAction == null) {
            return;
        }

        if (emoteReaction.isDeleteMessage()) {
            final MessageChannel channel = this.module.getDiscord().getTextChannelById(emoteReaction.getChannelId());
            if (channel != null) {
                channel.deleteMessageById(event.getMessageIdLong()).queue();
            }

        } else if (emoteReaction.isOneTimeUse()) {
            this.module.invalidateMessage(event.getMessageIdLong());
        }

        emoteAction.onEmote(event);
    }

    @SubscribeEvent
    public void onMessageDelete(final MessageDeleteEvent event) {
        this.module.invalidateMessage(event.getMessageIdLong());
    }
}
