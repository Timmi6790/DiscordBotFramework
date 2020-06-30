package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class JavaGamesCommand extends AbstractJavaStatsCommand {
    public JavaGamesCommand() {
        super("games", "Java Games", "[game] [stat]", "g");

        this.setDefaultPerms(true);

        this.addExampleCommands(
                "Global",
                "Global ExpEarned"
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Show all games
        if (commandParameters.getArgs().length == 0) {
            final EmbedBuilder message = this.getEmbedBuilder(commandParameters)
                    .setTitle("Java Games")
                    .setFooter("TIP: Run " + StatsBot.getCommandManager().getMainCommand() + " games <game> to see more details");

            this.getStatsModule().getJavaGames().values().stream()
                    .collect(Collectors.groupingBy(JavaGame::getCategory, TreeMap::new, Collectors.toList()))
                    .forEach((key, value) ->
                            message.addField(
                                    key,
                                    value.stream()
                                            .map(JavaGame::getName)
                                            .sorted(Comparator.naturalOrder())
                                            .collect(Collectors.joining(", ")),
                                    false
                            ));

            this.sendTimedMessage(commandParameters, message, 150);
            return CommandResult.SUCCESS;
        }

        // Game info
        final JavaGame game = this.getGame(commandParameters, 0);
        if (commandParameters.getArgs().length == 1) {
            final String stats = game.getStatNames()
                    .stream()
                    .map(stat -> stat.replace(" ", ""))
                    .collect(Collectors.joining(", "));
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Java Games - " + game.getName())
                            .addField("Wiki", "[" + game.getName() + "](" + game.getWikiUrl() + ")", false, !game.getWikiUrl().isEmpty())
                            .addField("Description", game.getDescription(), false, !game.getDescription().isEmpty())
                            .addField("Alias names", String.join(", ", game.getAliasNames()), false, game.getAliasNames().length > 0)
                            .addField("Stats (You don't need to type Achievement in front of it)", stats, false)
                            .setFooter("TIP: Run " + StatsBot.getCommandManager().getMainCommand() + " games " + game.getName() + " <stat> to see more details"),
                    90
            );

            return CommandResult.SUCCESS;
        }

        // Stat info
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Java Games - " + game.getName() + " - " + stat.getPrintName())
                        .addField("Description", stat.getDescription(), false, !stat.getDescription().isEmpty())
                        .addField("Alias names", String.join(", ", stat.getAliasNames()), false, stat.getAliasNames().length > 0)
                        .addField("Boards", String.join(", ", stat.getBoardNames()), false),
                150
        );
        return CommandResult.SUCCESS;
    }
}
