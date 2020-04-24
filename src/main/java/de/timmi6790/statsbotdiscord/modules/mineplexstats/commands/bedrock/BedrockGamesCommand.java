package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

public class BedrockGamesCommand extends AbstractBedrockStatsCommand {
    public BedrockGamesCommand() {
        super("bgames", "Bedrock games", "", "bg");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final List<String> games = new ArrayList<>(this.getStatsModule().getBedrockGames().values());
        games.sort(Comparator.naturalOrder());

        final StringJoiner description = new StringJoiner("\n");
        for (final String game : games) {
            description.add(game);
        }

        this.sendTimedMessage(
                commandParameters,
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Bedrock Games")
                        .setDescription(description.toString()),
                150
        );
        return CommandResult.SUCCESS;
    }
}
