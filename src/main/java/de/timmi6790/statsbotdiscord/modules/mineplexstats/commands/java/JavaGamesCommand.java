package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class JavaGamesCommand extends AbstractJavaStatsCommand {
    public JavaGamesCommand() {
        super("games", "MineplexStats - Java", "Shows all games", "[game] [stat]", "g");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final MineplexStatsModule module = (MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class);

        // Show all games
        if (commandParameters.getArgs().length == 0) {
            final Collection<JavaGame> games = module.getJavaGames().values();

            final TreeMap<String, List<String>> categories = new TreeMap<>();
            // Sort after category
            for (final JavaGame game : games) {
                if (!categories.containsKey(game.getCategory())) {
                    categories.put(game.getCategory(), new ArrayList<>());
                }

                categories.get(game.getCategory()).add(game.getName());
            }


            final EmbedBuilder message = UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                    .setTitle("Java Games");

            for (final Map.Entry<String, List<String>> entry : categories.entrySet()) {
                // Sort alphabetical
                entry.getValue().sort(Comparator.naturalOrder());

                final StringJoiner categoryGames = new StringJoiner(", ");
                for (final String game : entry.getValue()) {
                    categoryGames.add(game);
                }
                message.addField(entry.getKey(), categoryGames.toString(), false);
            }

            commandParameters.getDiscordChannel().sendMessage(message.build())
                    .delay(90, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();

            return CommandResult.SUCCESS;
        }

        // Game info
        final JavaGame game = this.getGame(commandParameters, 0);
        if (commandParameters.getArgs().length == 1) {
            final EmbedBuilder message = UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                    .setTitle("Java Games - " + game.getName());

            if (!game.getWikiUrl().isEmpty()) {
                message.addField("Wiki", "[" + game.getName() + "](" + game.getWikiUrl() + ")", false);
            }

            message.addField("Description", game.getDescription(), false);

            if (game.getAliasNames().length > 0) {
                message.addField("Alias names", String.join(", ", game.getAliasNames()), false);
            }

            message.addField("Stats", String.join(", ", game.getStatNames()), false);

            commandParameters.getDiscordChannel().sendMessage(message.build())
                    .delay(90, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();

            return CommandResult.SUCCESS;
        }

        // Stat info
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        final EmbedBuilder message = UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                .setTitle("Java Games - " + game.getName() + " - " + stat.getName())
                .addField("Pretty name", stat.getPrettyStat(), false)
                .addField("Description", stat.getDescription(), false);

        if (stat.getAliasNames().length > 0) {
            message.addField("Alias names", String.join(", ", stat.getAliasNames()), false);
        }

        message.addField("Boards", String.join(", ", stat.getBoardNames()), false);

        commandParameters.getDiscordChannel().sendMessage(message.build())
                .delay(90, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue();

        return CommandResult.SUCCESS;
    }
}
