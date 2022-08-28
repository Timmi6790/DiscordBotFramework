package de.timmi6790.discord_framework.module.modules.reactions.button.actions;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.cause.BaseCommandCause;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.StoredSlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import lombok.Data;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class CommandButtonAction implements ButtonAction {
    private final Class<? extends SlashCommand> commandClass;
    private final ParsedValues values;

    public CommandButtonAction(final Class<? extends SlashCommand> commandClass, final SlashCommandParameters commandParameters) {
        this.commandClass = commandClass;
        this.values = new ParsedValues(
                commandParameters.getOptions(),
                commandParameters.isGuildCommand(),
                commandParameters.getSubCommandName().orElse(null),
                commandParameters.getChannelDb().getDiscordId(),
                commandParameters.getGuildDb().getDiscordId(),
                commandParameters.getUserDb().getDiscordId()
        );
    }

    @Override
    public void onButtonClick(final ButtonInteractionEvent buttonClickEvent) {
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(SlashCommandModule.class)
                .getCommand(this.commandClass)
                .ifPresent(command -> command.executeCommand(this.values.getCommandParameters()));
    }

    @Data
    private static class ParsedValues {
        private final Map<String, OptionMapping> options;
        private final boolean guildCommand;
        private final String subCommandName;
        private final long channelDiscordId;
        private final long guildDiscordId;
        private final long userDiscordId;

        public SlashCommandParameters getCommandParameters() {
            final ModuleManager moduleManager = DiscordBot.getInstance().getModuleManager();
            return new StoredSlashCommandParameters(
                    DiscordBot.getInstance().getBaseShard(),
                    BaseCommandCause.EMOTES,
                    moduleManager.getModuleOrThrow(SlashCommandModule.class),
                    moduleManager.getModuleOrThrow(ChannelDbModule.class).getOrCreate(this.channelDiscordId, this.guildDiscordId),
                    moduleManager.getModuleOrThrow(UserDbModule.class).getOrCreate(this.userDiscordId),
                    this.options,
                    this.subCommandName,
                    this.guildCommand
            );
        }
    }
}
