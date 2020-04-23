package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.AbstractStatsCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.JavaGamesCommand;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public abstract class AbstractBedrockStatsCommand extends AbstractStatsCommand {
    private final static Pattern NAME_PATTERN = Pattern.compile("^.{3,32}$");

    public AbstractBedrockStatsCommand(final String name, final String description, final String syntax, final String... aliasNames) {
        super(name, "MineplexStats - Bedrock", description, syntax, aliasNames);
    }

    protected String getGame(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        final Optional<String> game = this.getStatsModule().getBedrockGame(name);
        if (game.isPresent()) {
            return game.get();
        }

        final List<String> similarNames = new ArrayList<>(this.getStatsModule().getSimilarBedrockGames(name, 0.6, 3));

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "game", command, new String[0], similarNames.toArray(new String[0]));

        throw new CommandReturnException();
    }

    protected String getPlayer(final CommandParameters commandParameters) {
        final String name = String.join(" ", commandParameters.getArgs());

        if (NAME_PATTERN.matcher(name).find()) {
            return name;
        }

        throw new CommandReturnException(
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Invalid Name")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a minecraft name.")
        );
    }
}
