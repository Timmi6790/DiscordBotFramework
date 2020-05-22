package de.timmi6790.statsbotdiscord.modules.core.commands.info;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesString;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;

public class HelpCommand extends AbstractCommand {
    public HelpCommand() {
        super("help", "Info", "In need of help", "[command]", "h");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // All info
        if (commandParameters.getArgs().length == 0) {
            final String mainCommand = StatsBot.getCommandManager().getMainCommand() + " ";
            final TreeMap<String, List<String>> categories = new TreeMap<>();
            for (final AbstractCommand command : StatsBot.getCommandManager().getCommands()) {
                if (!command.hasPermission(commandParameters)) {
                    continue;
                }

                if (!categories.containsKey(command.getCategory())) {
                    categories.put(command.getCategory(), new ArrayList<>());
                }

                final String upperCaseName = command.getName().substring(0, 1).toUpperCase() + command.getName().substring(1);
                categories.get(command.getCategory()).add(upperCaseName + ": " + MarkdownUtil.monospace(mainCommand + command.getName() + (command.getSyntax().length() == 0 ? "" : " " + command.getSyntax())));
            }


            final EmbedBuilder message = this.getEmbedBuilder(commandParameters)
                    .setTitle("Commands")
                    .setDescription("<> Required [] Optional | " + MarkdownUtil.bold("Don't use <> and [] in the actual command"));

            for (final Map.Entry<String, List<String>> entry : categories.entrySet()) {
                final StringJoiner commands = new StringJoiner("\n");
                for (final String line : entry.getValue()) {
                    commands.add(line);
                }

                message.addField(entry.getKey(), commands.toString(), false);
            }
            message.setFooter("TIP: Use " + StatsBot.getCommandManager().getMainCommand() + " help <command> to see more details");

            this.sendTimedMessage(commandParameters, message, 90);
            return CommandResult.SUCCESS;
        }

        // Command specific
        final AbstractCommand command = this.getCommand(commandParameters, 0);
        final EmbedBuilder message = this.getEmbedBuilder(commandParameters)
                .setTitle("Commands " + UtilitiesString.capitalize(command.getName()))
                .addField("Description", command.getDescription(), false)
                .addField("Alias Names", String.join(", ", command.getAliasNames()), false)
                .addField("Syntax", command.getSyntax(), false);

        this.sendTimedMessage(commandParameters, message, 90);
        return CommandResult.SUCCESS;
    }
}
