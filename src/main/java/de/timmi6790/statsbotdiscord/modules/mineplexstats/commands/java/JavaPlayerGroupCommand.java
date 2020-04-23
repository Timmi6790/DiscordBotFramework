package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.PictureTable;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JavaPlayerGroupCommand extends AbstractJavaStatsCommand {
    public JavaPlayerGroupCommand() {
        super("gplayer", "Group players", "<player> <group> <stat> [board] [date]", "gpl");

        this.setMinArgs(3);
        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final MineplexStatsModule module = this.getStatsModule();

        final String player = this.getPlayer(commandParameters, 0);
        final JavaGroup javaGroup = this.getJavaGroup(commandParameters, 1);
        final JavaStat stat = this.getJavaStat(javaGroup, commandParameters, 2);

        final List<JavaGame> statSpecificGames = javaGroup.getGames(stat);
        final JavaBoard board = this.getBoard(statSpecificGames.get(0), stat, commandParameters, 3);

        final ResponseModel responseModel = module.getMpStatsRestClient().getPlayerGroup(player, javaGroup.getGroup(), stat.getName(), board.getName());
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        final JavaGroupsPlayer groupStats = (JavaGroupsPlayer) responseModel;
        final JavaGroupsPlayer.Info playerStatsInfo = groupStats.getInfo();
        final Map<String, JavaGroupsPlayer.Stats> playerStats = groupStats.getStats();

        final CompletableFuture<BufferedImage> skinFuture = this.getPlayerSkin(playerStatsInfo.getUuid());

        final String[][] leaderboard = new String[statSpecificGames.size() + 1][3];
        leaderboard[0] = new String[]{"Game", "Score", "Position"};

        int highestUnixTime = 0;
        int index = 1;
        for (final JavaGame game : statSpecificGames) {
            String score = UNKNOWN_SCORE;
            String position = UNKNOWN_POSITION;
            if (playerStats.containsKey(game.getName())) {
                final JavaGroupsPlayer.Stats playerStat = playerStats.get(game.getName());

                score = this.getFormattedScore(stat, playerStat.getScore());
                position = String.valueOf(playerStat.getPosition());

                if (playerStat.getUnix() > highestUnixTime) {
                    highestUnixTime = playerStat.getUnix();
                }
            }
            leaderboard[index] = new String[]{game.getName(), score, position};
            index++;
        }

        BufferedImage skin;
        try {
            skin = skinFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            skin = null;
        }

        final String[] header = {playerStatsInfo.getName(), playerStatsInfo.getGroup(), playerStatsInfo.getPrettyStat(), playerStatsInfo.getBoard()};
        final PictureTable statsPicture = new PictureTable(header, this.getFormattedUnixTime(highestUnixTime), leaderboard, skin);
        final Optional<InputStream> picture = statsPicture.getPlayerPicture();

        if (picture.isPresent()) {
            commandParameters.getDiscordChannel().sendFile(picture.get(), String.join("-", header) + "-" + highestUnixTime + ".png").queue();
            return CommandResult.SUCCESS;
        }

        this.sendErrorMessage(commandParameters, "Error while creating picture.");
        return CommandResult.ERROR;
    }
}
