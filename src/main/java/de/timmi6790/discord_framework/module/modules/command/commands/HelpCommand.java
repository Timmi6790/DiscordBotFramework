package de.timmi6790.discord_framework.module.modules.command.commands;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.*;
import de.timmi6790.discord_framework.module.modules.command.utilities.ArgumentUtilities;
import de.timmi6790.discord_framework.module.modules.command.utilities.MessageUtilities;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.commons.StringUtilities;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HelpCommand extends Command {
    private static final String DEFAULT_CATEGORY = "";

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final CommandModule commandModule;

    public HelpCommand(final CommandModule commandModule, final EventModule eventModule) {
        super("help", commandModule, eventModule);

        this.commandModule = commandModule;

        this.addProperties(
                new CategoryProperty("Info"),
                new DescriptionProperty("In need of help"),
                new SyntaxProperty("[command]"),
                new AliasNamesProperty("h")
        );
    }

    private String getDescription(final Command command) {
        return command.getPropertyValueOrDefault(DescriptionProperty.class, () -> "");
    }

    private String getSyntax(final Command command) {
        return command.getPropertyValueOrDefault(SyntaxProperty.class, () -> "");
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
        final MultiEmbedBuilder message = commandParameters.getEmbedBuilder()
                .setTitle("Commands")
                .setDescription("<> Required [] Optional | " + MarkdownUtil.bold("Don't use <> and [] in the actual command"))
                .setFooterFormat(
                        "TIP: Use %s %s <command> to see more details",
                        this.commandModule.getMainCommand(),
                        this.getName()
                );

        // Group all commands via their category
        final Map<String, List<Command>> sortedCommands = new HashMap<>();
        for (final Command command : this.commandModule.getCommands(command -> command.canExecute(commandParameters))) {
            sortedCommands.computeIfAbsent(
                    command.getPropertyValueOrDefault(CategoryProperty.class, () -> DEFAULT_CATEGORY),
                    k -> new ArrayList<>()
            ).add(command);
        }

        // Sort the command after name
        for (final Map.Entry<String, List<Command>> entry : sortedCommands.entrySet()) {
            entry.getValue().sort(Comparator.comparing(Command::getName));

            final StringJoiner lines = new StringJoiner("\n");
            for (final Command command : entry.getValue()) {
                final String commandSyntax = this.getSyntax(command);
                final String syntax = commandSyntax.length() == 0 ? "" : " " + commandSyntax;
                lines.add(String.format(
                        "%s %s",
                        MarkdownUtil.monospace(this.commandModule.getMainCommand() + command.getName() + syntax),
                        this.getDescription(command)
                ));
            }

            message.addField(
                    entry.getKey(),
                    lines.toString()
            );
        }

        commandParameters.sendMessage(message);
        return BaseCommandResult.SUCCESSFUL;
    }

    /**
     * Shows the help message for a specific command declared at argument position 0
     *
     * @param commandParameters the command parameters
     * @return the command result
     */
    protected CommandResult showCommandHelpMessage(final CommandParameters commandParameters) {
        final Command command = ArgumentUtilities.getCommand(
                commandParameters,
                this.commandModule,
                commandParameters.getArg(0)
        ).orElseThrow(CommandReturnException::new);
        if (!command.canExecute(commandParameters)) {
            MessageUtilities.sendMissingPermissionsMessage(commandParameters);
            return BaseCommandResult.SUCCESSFUL;
        }

        final String exampleCommands = String.join(
                "\n",
                command.getPropertyValueOrDefault(ExampleCommandsProperty.class, () -> new String[0])
        );
        final String description = this.getDescription(command);
        final String syntax = this.getSyntax(command);
        final String aliasNames = String.join(
                ", ",
                command.getPropertyValueOrDefault(AliasNamesProperty.class, () -> new String[0])
        );
        final MultiEmbedBuilder message = commandParameters.getEmbedBuilder()
                .setTitle("Commands " + StringUtilities.capitalize(command.getName()))
                .addField("Description", description, false, !description.isEmpty())
                .addField("Alias Names", aliasNames, false, !aliasNames.isEmpty())
                .addField("Syntax", syntax, false, !syntax.isEmpty())
                .addField("Example Commands", exampleCommands, false, !exampleCommands.isEmpty());

        commandParameters.sendMessage(message);
        return BaseCommandResult.SUCCESSFUL;
    }
}
