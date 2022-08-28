package de.timmi6790.discord_framework.module.modules.reactions.button.listeners;

import de.timmi6790.discord_framework.module.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReaction;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReactionModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;

import java.util.Optional;

@RequiredArgsConstructor
public class ButtonReactionListener {
    private final ButtonReactionModule module;

    @SubscribeEvent
    public void onButtonClick(final ButtonInteractionEvent event) {
        final Optional<ButtonReaction> reactionOpt = this.module.getButtonReaction(event.getMessageIdLong());
        if (reactionOpt.isEmpty()) {
            return;
        }

        final ButtonReaction reaction = reactionOpt.get();
        if (!reaction.getUsers().contains(event.getUser().getIdLong())) {
            return;
        }

        final ButtonAction action = reaction.getButtons().get(event.getComponentId());
        if (action == null) {
            return;
        }

        if (reaction.isDeleteMessage()) {
            final Message message = event.getMessage();
            if (message != null) {
                message.delete().queue();
            }
        } else if (reaction.isOneTimeUse()) {
            this.module.invalidateMessage(event.getMessageIdLong());
        }

        action.onButtonClick(event);
    }

    @SubscribeEvent
    public void onMessageDelete(final MessageDeleteEvent event) {
        this.module.invalidateMessage(event.getMessageIdLong());
    }
}
