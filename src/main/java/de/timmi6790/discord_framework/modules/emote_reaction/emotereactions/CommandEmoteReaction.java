package de.timmi6790.discord_framework.modules.emote_reaction.emotereactions;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandCause;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import lombok.Data;

@Data
public class CommandEmoteReaction implements AbstractEmoteReaction {
    // TODO: Find a better way, it is stupid to save the entire commandParameter object in here

    private final Class<? extends AbstractCommand> command;
    private final CommandParameters commandParameters;

    public CommandEmoteReaction(final AbstractCommand command, final CommandParameters commandParameters) {
        this.command = command.getClass();
        this.commandParameters = commandParameters;
    }

    @Override
    public void onEmote() {
        DiscordBot.getCommandManager()
                .getCommand(this.command)
                .ifPresent(command -> command.runCommand(this.commandParameters, CommandCause.EMOTES));
    }
}
