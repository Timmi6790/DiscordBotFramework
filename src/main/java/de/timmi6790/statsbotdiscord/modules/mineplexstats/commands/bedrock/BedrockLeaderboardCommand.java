package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock;

import de.timmi6790.statsbotdiscord.datatypes.ListBuilder;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.EmoteReactionMessage;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.PictureTable;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.bedrock.BedrockGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.bedrock.BedrockLeaderboard;

import java.util.ArrayList;
import java.util.List;

public class BedrockLeaderboardCommand extends AbstractBedrockStatsCommand {
    private final static int ARG_POS_START_POS = 1;
    private final static int ARG_POS_END_POS = 2;

    private final static int LEADERBOARD_UPPER_LIMIT = 100;

    public BedrockLeaderboardCommand() {
        super("bleaderboard", "Bedrock Leaderboard", "<game> [start] [end] [date]", "bl", "blb");

        this.setDefaultPerms(true);
        this.setMinArgs(1);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final BedrockGame game = this.getGame(commandParameters, 0);
        final int startPos = this.getStartPosition(commandParameters, ARG_POS_START_POS, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPosition(startPos, commandParameters, ARG_POS_END_POS, LEADERBOARD_UPPER_LIMIT);
        final long unixTime = this.getUnixTime(commandParameters, 3);

        final ResponseModel responseModel = this.getStatsModule().getMpStatsRestClient().getBedrockLeaderboard(game.getName(), startPos, endPos, unixTime);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        // Parse the data into the image maker format
        final List<BedrockLeaderboard.Leaderboard> leaderboardResponse = ((BedrockLeaderboard) responseModel).getLeaderboard();
        final String[][] leaderboard = new ListBuilder<String[]>(() -> new ArrayList<>(leaderboardResponse.size() + 1))
                .add(new String[]{"Player", "Score", "Position"})
                .addAll(leaderboardResponse.stream()
                        .map(data -> new String[]{data.getName(), this.getFormattedNumber(data.getScore()), String.valueOf(data.getPosition())}))
                .build()
                .toArray(new String[0][3]);

        final BedrockLeaderboard.Info leaderboardInfo = ((BedrockLeaderboard) responseModel).getInfo();

        final String[] header = {"Bedrock " + leaderboardInfo.getGame()};

        // Emotes
        final int rowDistance = endPos - startPos;
        final int fastRowDistance = leaderboardInfo.getTotalLength() * 50 / 100;

        if (Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1 > commandParameters.getArgs().length) {
            final String[] newArgs = new String[Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1];
            System.arraycopy(commandParameters.getArgs(), 0, newArgs, 0, commandParameters.getArgs().length);
            commandParameters.setArgs(newArgs);
        }

        return this.sendPicture(
                commandParameters,
                new PictureTable(header, this.getFormattedUnixTime(leaderboardInfo.getUnix()), leaderboard).getPlayerPicture(),
                String.join("-", header) + "-" + leaderboardInfo.getUnix(),
                new EmoteReactionMessage(
                        this.getLeaderboardEmotes(commandParameters, rowDistance, fastRowDistance, startPos, endPos,
                                leaderboardInfo.getTotalLength(), ARG_POS_START_POS, ARG_POS_END_POS),
                        commandParameters.getEvent().getAuthor().getIdLong(),
                        commandParameters.getEvent().getChannel().getIdLong()
                )
        );
    }
}

