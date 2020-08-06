package de.timmi6790.external_modules.mineplexstats.commands.java;

import de.timmi6790.discord_framework.datatypes.BiggestLong;
import de.timmi6790.discord_framework.datatypes.ListBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.external_modules.mineplexstats.picture.PictureTable;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JavaPlayerGroupCommand extends AbstractJavaStatsCommand {
    public JavaPlayerGroupCommand() {
        super("gplayer", "Java player group stats", "<player> <group> <stat> [board] [date]", "gpl");

        this.setCategory("MineplexStats - Java - Group");

        this.addProperties(
                new MinArgCommandProperty(3),
                new ExampleCommandsCommandProperty(
                        "nwang888 MixedArcade wins",
                        "nwang888 MixedArcade wins yearly",
                        "nwang888 MixedArcade wins global 1/30/2020"
                )
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse input
        final String player = this.getPlayer(commandParameters, 0);
        final JavaGroup javaGroup = this.getJavaGroup(commandParameters, 1);
        final JavaStat stat = this.getJavaStat(javaGroup, commandParameters, 2);

        // TODO: Fix me: Possible bug when not all game stats have the same boards
        final List<JavaGame> statSpecificGames = javaGroup.getGames(stat);
        final JavaBoard board = this.getBoard(statSpecificGames.get(0), stat, commandParameters, 3);
        final long unixTime = this.getUnixTime(commandParameters, 4);

        final ResponseModel responseModel = this.getStatsModule().getMpStatsRestClient().getPlayerGroup(player, javaGroup.getGroup(), stat.getName(), board.getName(), unixTime);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        // Parse data to image
        final JavaGroupsPlayer groupStats = (JavaGroupsPlayer) responseModel;
        final JavaGroupsPlayer.Info playerStatsInfo = groupStats.getInfo();
        final CompletableFuture<BufferedImage> skinFuture = this.getPlayerSkin(playerStatsInfo.getUuid());

        final BiggestLong highestUnixTime = new BiggestLong(0);
        final String[][] leaderboard = new ListBuilder<String[]>(() -> new ArrayList<>(statSpecificGames.size() + 1))
                .add(new String[]{"Game", "Score", "Position"})
                .addAll(statSpecificGames.stream()
                        .map(game -> Optional.ofNullable(groupStats.getStats().get(game.getName()))
                                .map(playerStat -> {
                                    highestUnixTime.tryNumber(playerStat.getUnix());
                                    return new String[]{game.getName(), this.getFormattedScore(stat, playerStat.getScore()), String.valueOf(playerStat.getPosition())};
                                })
                                .orElse(new String[]{game.getName(), UNKNOWN_SCORE, UNKNOWN_POSITION}))
                )
                .build()
                .toArray(new String[0][3]);

        BufferedImage skin;
        try {
            skin = skinFuture.get();
        } catch (final InterruptedException | ExecutionException ignore) {
            skin = null;
        }

        final String[] header = {playerStatsInfo.getName(), playerStatsInfo.getGroup(), playerStatsInfo.getStat(), playerStatsInfo.getBoard()};
        return this.sendPicture(
                commandParameters,
                new PictureTable(header, this.getFormattedUnixTime(highestUnixTime.get()), leaderboard, skin).getPlayerPicture(),
                String.join("-", header) + "-" + highestUnixTime
        );
    }
}
