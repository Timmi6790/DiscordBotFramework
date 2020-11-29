package de.timmi6790.discord_framework.modules.command.commands;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.timmi6790.commons.utilities.StringUtilities;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
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
                .setFooterFormat("TIP: Use %s help <command> to see more details", getCommandModule().getMainCommand());

        final Multimap<String, AbstractCommand> sortedCommands = MultimapBuilder.treeKeys()
                .arrayListValues()
                .build();

        // Group all commands via their category
        for (final AbstractCommand command : getCommandModule().getCommands()) {
            if (command.hasPermission(commandParameters)) {
                sortedCommands.put(command.getCategory(), command);
            }
        }

        // Sort the command after name
        for (final Map.Entry<String, Collection<AbstractCommand>> entry : sortedCommands.asMap().entrySet()) {
            final List<AbstractCommand> commands = new ArrayList<>(entry.getValue());
            commands.sort(Comparator.comparing(AbstractCommand::getName));

            final StringJoiner lines = new StringJoiner("\n");
            for (final AbstractCommand command : commands) {
                final String syntax = command.getSyntax().length() == 0 ? "" : " " + command.getSyntax();
                lines.add(String.format(
                        "%s %s",
                        MarkdownUtil.monospace(getCommandModule().getMainCommand() + command.getName() + syntax),
                        command.getDescription()
                ));
            }

            message.addField(
                    entry.getKey(),
                    lines.toString()
            );
        }

        this.sendTimedMessage(commandParameters, message, 150);
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

        this.sendTimedMessage(commandParameters, message, 90);

        return CommandResult.SUCCESS;
    }
}
