package de.timmi6790.discord_framework.module.modules.command_old.commands;

import de.timmi6790.discord_framework.module.modules.command_old.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command_old.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command_old.CommandResult;
import de.timmi6790.discord_framework.module.modules.command_old.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.commons.StringUtilities;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class HelpCommand extends AbstractCommand {
    /**
     * Instantiates a new Help command.
     */
    public HelpCommand() {
        super("help", "Info", "In need of help", "[command]", "h");

        this.addProperties(
                new ExampleCommandsCommandProperty(
                        "help"
                )
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // All info
        if (commandParameters.getArgs().length == 0) {
            return this.showAllCommandHelpMessage(commandParameters);
        }

        // Command specific
        return this.showCommandHelpMessage(commandParameters);
    }

    /**
     * Shows a general help message to the user with all commands he can execute. The help message is split into the the
     * command categories.
     *
     * @param commandParameters the command parameters
     * @return the command result
     */
    protected CommandResult showAllCommandHelpMessage(final CommandParameters commandParameters) {
        final MultiEmbedBuilder message = this.getEmbedBuilder(commandParameters)
                .setTitle("Commands")
                .setDescription("<> Required [] Optional | " + MarkdownUtil.bold("Don't use <> and [] in the actual command"))
                .setFooterFormat(
                        "TIP: Use %s %s <command> to see more details",
                        this.getCommandModule().getMainCommand(),
                        this.getName()
                );

        // Group all commands via their category
        final Map<String, List<AbstractCommand>> sortedCommands = new HashMap<>();
        for (final AbstractCommand command : this.getCommandModule().getCommandsWithPerms(commandParameters)) {
            sortedCommands.computeIfAbsent(command.getCategory(), k -> new ArrayList<>()).add(command);
        }

        // Sort the command after name
        for (final Map.Entry<String, List<AbstractCommand>> entry : sortedCommands.entrySet()) {
            entry.getValue().sort(Comparator.comparing(AbstractCommand::getName));

            final StringJoiner lines = new StringJoiner("\n");
            for (final AbstractCommand command : entry.getValue()) {
                final String syntax = command.getSyntax().length() == 0 ? "" : " " + command.getSyntax();
                lines.add(String.format(
                        "%s %s",
                        MarkdownUtil.monospace(this.getCommandModule().getMainCommand() + command.getName() + syntax),
                        command.getDescription()
                ));
            }

            message.addField(
                    entry.getKey(),
                    lines.toString()
            );
        }

        this.sendTimedMessage(commandParameters, message);
        return CommandResult.SUCCESS;
    }

    /**
     * Shows the help message for a specific command declared at argument position 0
     *
     * @param commandParameters the command parameters
     * @return the command result
     */
    protected CommandResult showCommandHelpMessage(final CommandParameters commandParameters) {
        final AbstractCommand command = this.getCommandThrow(commandParameters, 0);
        if (!command.hasPermission(commandParameters)) {
            this.sendMissingPermissionMessage(commandParameters);
            return CommandResult.SUCCESS;
        }

        final String exampleCommands = String.join("\n", command.getFormattedExampleCommands());
        final MultiEmbedBuilder message = this.getEmbedBuilder(commandParameters)
                .setTitle("Commands " + StringUtilities.capitalize(command.getName()))
                .addField("Description", command.getDescription(), false, !command.getDescription().isEmpty())
                .addField("Alias Names", String.join(", ", command.getAliasNames()), false, command.getAliasNames().length != 0)
                .addField("Syntax", command.getSyntax(), false, !command.getSyntax().isEmpty())
                .addField("Example Commands", exampleCommands, false, !exampleCommands.isEmpty());

        this.sendTimedMessage(commandParameters, message);

        return CommandResult.SUCCESS;
    }
}
