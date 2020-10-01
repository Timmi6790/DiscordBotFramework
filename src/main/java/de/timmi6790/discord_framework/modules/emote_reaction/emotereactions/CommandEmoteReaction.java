package de.timmi6790.discord_framework.modules.emote_reaction.emotereactions;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandCause;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.Data;

@Data
public class CommandEmoteReaction implements AbstractEmoteReaction {
    private final Class<? extends AbstractCommand> commandClass;
    private final Values values;

    public CommandEmoteReaction(final AbstractCommand commandClass, final CommandParameters commandParameters) {
        this.commandClass = (Class<? extends AbstractCommand>) commandClass.getClass();
        this.values = new Values(
                commandParameters.getArgs(),
                commandParameters.isGuildCommand(),
                commandParameters.getChannelDb().getDiscordId(),
                commandParameters.getGuildDb().getDiscordId(),
                commandParameters.getUserDb().getDiscordId()
        );
    }

    @Override
    public void onEmote() {
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(CommandModule.class)
                .getCommand(this.commandClass)
                .ifPresent(command -> command.runCommand(this.values.getCommandParameters()));
    }

    @Data
    private static class Values {
        private final String[] args;
        private final boolean guildCommand;
        private final long channelDiscordId;
        private final long guildDiscordId;
        private final long userDiscordId;

        public CommandParameters getCommandParameters() {
            final ModuleManager moduleManager = DiscordBot.getInstance().getModuleManager();
            return new CommandParameters(
                    String.join(" ", this.args),
                    this.args,
                    this.guildCommand,
                    CommandCause.EMOTES,
                    moduleManager.getModuleOrThrow(ChannelDbModule.class).getOrCreate(this.channelDiscordId, this.guildDiscordId),
                    moduleManager.getModuleOrThrow(UserDbModule.class).getOrCreate(this.userDiscordId)
            );
        }
    }
}
