package de.timmi6790.external_modules.mineplexstats.commands.bedrock;

import de.timmi6790.discord_framework.datatypes.BiggestLong;
import de.timmi6790.discord_framework.datatypes.ListBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.external_modules.mineplexstats.picture.PictureTable;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock.BedrockPlayerStats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class BedrockPlayerCommand extends AbstractBedrockStatsCommand {
    public BedrockPlayerCommand() {
        super("bplayer", "Bedrock player stats", "<player>", "bpl");

        this.setDefaultPerms(true);
        this.setMinArgs(1);

        this.addExampleCommands(
                "xottic7"
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Arg parse
        final String player = this.getPlayer(commandParameters, 0);

        final ResponseModel responseModel = this.getStatsModule().getMpStatsRestClient().getBedrockPlayerStats(player);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        final BedrockPlayerStats bedrockStats = (BedrockPlayerStats) responseModel;
        final BedrockPlayerStats.Info playerStatsInfo = bedrockStats.getInfo();
        final Map<String, BedrockPlayerStats.Stats> playerStats = bedrockStats.getStats();

        // Parse board data
        final BiggestLong highestUnixTime = new BiggestLong(0);
        final String[][] leaderboard = new ListBuilder<String[]>(() -> new ArrayList<>(bedrockStats.getStats().size() + 1))
                .add(new String[]{"Game", "Score", "Position"})
                .addAll(bedrockStats.getStats().keySet()
                        .stream()
                        .sorted(Comparator.naturalOrder())
                        .map(game -> {
                            final BedrockPlayerStats.Stats playerStat = playerStats.get(game);
                            highestUnixTime.tryNumber(playerStat.getUnix());
                            return new String[]{game, this.getFormattedNumber(playerStat.getScore()), String.valueOf(playerStat.getPosition())};
                        }))
                .build()
                .toArray(new String[0][3]);

        final String[] header = {playerStatsInfo.getName() + " Bedrock"};
        return this.sendPicture(
                commandParameters,
                new PictureTable(header, this.getFormattedUnixTime(highestUnixTime.get()), leaderboard).getPlayerPicture(),
                String.join("-", header) + "-" + highestUnixTime
        );
    }
}
