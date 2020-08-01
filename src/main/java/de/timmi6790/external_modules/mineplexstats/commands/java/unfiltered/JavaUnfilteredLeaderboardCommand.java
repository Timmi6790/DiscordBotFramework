package de.timmi6790.external_modules.mineplexstats.commands.java.unfiltered;

import de.timmi6790.discord_framework.datatypes.ListBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.external_modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.external_modules.mineplexstats.picture.PictureTable;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaLeaderboard;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaStat;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class JavaUnfilteredLeaderboardCommand extends AbstractJavaStatsCommand {
    private static final int ARG_POS_BOARD_POS = 2;
    private static final int ARG_POS_START_POS = 3;
    private static final int ARG_POS_END_POS = 4;

    private static final int LEADERBOARD_UPPER_LIMIT = 1_000;

    public JavaUnfilteredLeaderboardCommand() {
        super("unfilteredLeaderboard", "Java Unfiltered Leaderboard", "<game> <stat> [board] [start] [end] [date]", "ulb");

        this.setCategory("MineplexStats - Java - Unfiltered");
        this.setDefaultPerms(true);
        this.setMinArgs(2);

        this.addExampleCommands(
                "Global ExpEarned",
                "Global ExpEarned daily",
                "Global ExpEarned global 20 40",
                "Global ExpEarned global 20 40 1/30/2020"
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final JavaGame game = this.getGame(commandParameters, 0);
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        final JavaBoard board = this.getBoard(game, commandParameters, ARG_POS_BOARD_POS);
        final int startPos = this.getStartPosition(commandParameters, ARG_POS_START_POS, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPosition(startPos, commandParameters, ARG_POS_END_POS, LEADERBOARD_UPPER_LIMIT);
        final long unixTime = this.getUnixTime(commandParameters, 5);

        final ResponseModel responseModel = this.getStatsModule().getMpStatsRestClient().getJavaLeaderboard(game.getName(), stat.getName(), board.getName(),
                startPos, endPos, unixTime, false);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        // Parse data to image generator
        final JavaLeaderboard leaderboardResponse = (JavaLeaderboard) responseModel;
        final String[][] leaderboard = new ListBuilder<String[]>(() -> new ArrayList<>(leaderboardResponse.getLeaderboard().size() + 1))
                .add(new String[]{"Player", "Score", "Position"})
                .addAll(leaderboardResponse.getLeaderboard()
                        .stream()
                        .map(data -> new String[]{data.getName(), this.getFormattedScore(stat, data.getScore()), String.valueOf(data.getPosition())})
                        .collect(Collectors.toList()))
                .build()
                .toArray(new String[0][3]);

        final JavaLeaderboard.Info leaderboardInfo = leaderboardResponse.getInfo();
        final String[] header = {leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard(), "UNFILTERED"};

        // Emote Reaction
        final int rowDistance = endPos - startPos;
        final int fastRowDistance = leaderboardInfo.getTotalLength() * 10 / 100;

        // Create a new args array if the old array has no positions
        if (Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1 > commandParameters.getArgs().length) {
            final String[] newArgs = new String[Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1];
            newArgs[ARG_POS_BOARD_POS] = board.getName();

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