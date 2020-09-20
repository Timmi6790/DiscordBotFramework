package de.timmi6790.discord_framework.modules.command.commands;

import de.timmi6790.commons.utilities.StringUtilities;
import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCommand<CommandModule> {
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
            final String mainCommand = this.getModule().getModuleOrThrow(CommandModule.class).getMainCommand();
            final MultiEmbedBuilder message = getEmbedBuilder(commandParameters)
                    .setTitle("Commands")
                    .setDescription("<> Required [] Optional | " + MarkdownUtil.bold("Don't use <> and [] in the actual command"))
                    .setFooter("TIP: Use " + this.getModule().getMainCommand() + " help <command> to see more details");

            this.getModule().getCommands()
                    .stream()
                    .filter(command -> command.hasPermission(commandParameters))
                    .collect(Collectors.groupingBy(AbstractCommand::getCategory, TreeMap::new, Collectors.toList()))
                    .forEach((key, value) ->
                            message.addField(
                                    key,
                                    value.stream()
                                            .sorted(Comparator.comparing(AbstractCommand::getName))
                                            .map(command -> {
                                                final String syntax = command.getSyntax().length() == 0 ? "" : " " + command.getSyntax();
                                                return MarkdownUtil.monospace(mainCommand + command.getName() + syntax) + " " + command.getDescription();
                                            })
                                            .collect(Collectors.joining("\n")),
                                    false
                            )
                    );

            sendTimedMessage(commandParameters, message, 150);
            return CommandResult.SUCCESS;
        }


        // Command specific
        final AbstractCommand<?> command = this.getCommandThrow(commandParameters, 0);
        final String exampleCommands = String.join("\n", command.getFormattedExampleCommands());

        final MultiEmbedBuilder message = getEmbedBuilder(commandParameters)
                .setTitle("Commands " + StringUtilities.capitalize(command.getName()))
                .addField("Description", command.getDescription(), false, !command.getDescription().isEmpty())
                .addField("Alias Names", String.join(", ", command.getAliasNames()), false, command.getAliasNames().length != 0)
                .addField("Syntax", command.getSyntax(), false, !command.getSyntax().isEmpty())
                .addField("Example Commands", exampleCommands, false, !exampleCommands.isEmpty());

        sendTimedMessage(commandParameters, message, 90);

        return CommandResult.SUCCESS;
    }
}
