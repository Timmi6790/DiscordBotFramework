package de.timmi6790.discord_framework.module.modules.slashcommand.commands;

import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.CommandOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;

public class HelpSlashCommand extends SlashCommand {
    private static final String DEFAULT_CATEGORY = "";

    private final Option<SlashCommand> commandOption;

    private final SlashCommandModule commandModule;

    public HelpSlashCommand(final SlashCommandModule commandModule) {
        super("help", "In need of help");

        this.commandModule = commandModule;

        final List<SlashCommand> commands = new ArrayList<>(commandModule.getCommands().values());
        commands.add(this);

        this.commandOption = new CommandOption(commandModule, "command", "Command name")
                .addTypeOptions(commands)
                .setRequired(false);
        this.addOptions(
                this.commandOption
        );

        this.addProperties(
                new CategoryProperty("Info"),
                new SyntaxProperty("[command]"),
                new AliasNamesProperty("h")
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

                    // Group all commands via their category
                    final Map<String, List<SlashCommand>> sortedCommands = new HashMap<>();
                    for (final SlashCommand command : this.commandModule.getCommands(command -> command.canExecute(parameters))) {
                        sortedCommands.computeIfAbsent(
                                command.getPropertyValueOrDefault(CategoryProperty.class, () -> DEFAULT_CATEGORY),
                                k -> new ArrayList<>()
                        ).add(command);
                    }

                    // Sort the command after name
                    for (final Map.Entry<String, List<SlashCommand>> entry : sortedCommands.entrySet()) {
                        entry.getValue().sort(Comparator.comparing(SlashCommand::getName));

                        final StringJoiner lines = new StringJoiner("\n");
                        for (final SlashCommand command : entry.getValue()) {
                            final String commandSyntax = this.getSyntax(command);
                            final String syntax = commandSyntax.length() == 0 ? "" : " " + commandSyntax;
                            lines.add(String.format(
                                    "/%s %s",
                                    MarkdownUtil.monospace(command.getName() + syntax),
                                    command.getDescription()
                            ));
                        }

                        message.addField(
                                entry.getKey(),
                                lines.toString()
                        );
                    }

                    parameters.sendMessage(message);
                }
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    private String getSyntax(final SlashCommand command) {
        return command.getPropertyValueOrDefault(SyntaxProperty.class, () -> "");
    }
}
