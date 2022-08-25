package de.timmi6790.discord_framework.module.modules.slashcommand.commands;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.CommandOption;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.ArrayList;
import java.util.List;

public class HelpSlashCommand extends SlashCommand {
    private final Option<SlashCommand> commandOption;

    private final SlashCommandModule commandModule;

    public HelpSlashCommand(final SlashCommandModule commandModule) {
        super("help", "Help in all needs");

        this.commandModule = commandModule;

        this.setAliases("h");

        final List<SlashCommand> commands = new ArrayList<>(commandModule.getCommands().values());
        commands.add(this);

        this.commandOption = new CommandOption(commandModule, "command", "Command name")
                .addTypeOptions(commands)
                .setRequired(false);
        this.addOptions(
                this.commandOption
        );
    }

    @Override
    public CommandResult onCommand(final SlashCommandParameters parameters) {
        parameters.getOption(this.commandOption).ifPresentOrElse(
                slashCommand -> {
                    final MultiEmbedBuilder message = new MultiEmbedBuilder()
                            .setTitle("Commands")
                            .setDescription("<> Required [] Optional | " + MarkdownUtil.bold("Don't use <> and [] in the actual command"))
                            .addField("Command", slashCommand.getName())
                            .setFooterFormat(
                                    "TIP: Use /%s <command> to see more details",
                                    this.getName()
                            );

                    parameters.sendMessage(message);
                },
                () -> {
                    final MultiEmbedBuilder message = new MultiEmbedBuilder()
                            .setTitle("Commands")
                            .setDescription("<> Required [] Optional | " + MarkdownUtil.bold("Don't use <> and [] in the actual command"))
                            .setFooterFormat(
                                    "TIP: Use /%s <command> to see more details",
                                    this.getName()
                            );

                    parameters.sendMessage(message);
                }
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    private String getDescription(final Command command) {
        return command.getPropertyValueOrDefault(DescriptionProperty.class, () -> "");
    }

    private String getSyntax(final Command command) {
        return command.getPropertyValueOrDefault(SyntaxProperty.class, () -> "");
    }
}
