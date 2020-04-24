package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.PictureTable;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.bedrock.BedrockPlayerStats;

import java.io.InputStream;
import java.util.*;

public class BedrockPlayerCommand extends AbstractBedrockStatsCommand {
    public BedrockPlayerCommand() {
        super("bplayer", "Bedrock player stats", "<player>", "bpl");

        this.setDefaultPerms(true);
        this.setMinArgs(1);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String player = this.getPlayer(commandParameters);
        final ResponseModel responseModel = this.getStatsModule().getMpStatsRestClient().getBedrockPlayerStats(player);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        final BedrockPlayerStats bedrockStats = (BedrockPlayerStats) responseModel;
        final BedrockPlayerStats.Info playerStatsInfo = bedrockStats.getInfo();
        final Map<String, BedrockPlayerStats.Stats> playerStats = bedrockStats.getStats();

        final List<String> bedrockGames = new ArrayList<>(playerStats.keySet());
        bedrockGames.sort(Comparator.naturalOrder());

        final String[][] leaderboard = new String[bedrockGames.size() + 1][3];
        leaderboard[0] = new String[]{"Game", "Score", "Position"};

        int highestUnixTime = 0;
        int index = 1;
        for (final String game : bedrockGames) {
            final BedrockPlayerStats.Stats playerStat = playerStats.get(game);

            final String score = this.getFormattedNumber(playerStat.getScore());
            final String position = String.valueOf(playerStat.getPosition());

            if (playerStat.getUnix() > highestUnixTime) {
                highestUnixTime = playerStat.getUnix();
            }

            leaderboard[index] = new String[]{game, score, position};
            index++;
        }

        final String[] header = {playerStatsInfo.getName() + " Bedrock"};
        final PictureTable statsPicture = new PictureTable(header, this.getFormattedUnixTime(highestUnixTime), leaderboard);
        final Optional<InputStream> picture = statsPicture.getPlayerPicture();

        if (picture.isPresent()) {
            commandParameters.getDiscordChannel().sendFile(picture.get(), String.join("-", header) + "-" + highestUnixTime + ".png").queue();
            return CommandResult.SUCCESS;
        }

        this.sendErrorMessage(commandParameters, "Error while creating picture.");
        return CommandResult.ERROR;
    }
}
