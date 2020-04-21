package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.PictureTable;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaPlayerStats;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;
import net.dv8tion.jda.api.Permission;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class JavaPlayerStatsCommand extends AbstractJavaStatsCommand {
    public JavaPlayerStatsCommand() {
        super("player", "Player stats", "<player> <game> [board] [date]", "pl");

        this.addDiscordPermission(Permission.MESSAGE_ATTACH_FILES);
        this.setMinArgs(2);
        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String player = this.getPlayer(commandParameters, 0);
        final JavaGame javaGame = this.getGame(commandParameters, 1);
        final JavaBoard board = this.getBoard(javaGame, commandParameters, 2);

        final MineplexStatsModule module = this.getStatsModule();
        final ResponseModel responseModel = module.getMpStatsRestClient().getJavaPlayerStats(player, javaGame.getName(), board.getName());
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        final JavaPlayerStats playerStats = (JavaPlayerStats) responseModel;
        final JavaPlayerStats.PlayerStatsInfo playerStatsInfo = playerStats.getInfo();

        final CompletableFuture<BufferedImage> skinFuture = this.getPlayerSkin(playerStatsInfo.getUuid());

        final JavaGame game = module.getJavaGame(playerStatsInfo.getGame()).get();
        final Map<String, JavaPlayerStats.PlayerStatsStats> stats = playerStats.getStats();

        int heighestUnixTime = 0;
        final String[][] leaderboard = new String[game.getStats().size() + 1][3];
        leaderboard[0] = new String[]{"Category", "Score", "Position"};

        final Map<String, JavaStat> gameStats = game.getStats();
        int index = 1;
        for (final String statName : game.getStatNames()) {
            final JavaStat gameStat = gameStats.get(statName.toLowerCase());
            String score = UNKNOWN_SCORE;
            String position = UNKNOWN_POSITION;
            if (stats.containsKey(gameStat.getName())) {
                final JavaPlayerStats.PlayerStatsStats stat = stats.get(gameStat.getName());

                score = this.getFormattedScore(gameStat, stat.getScore());
                position = String.valueOf(stat.getPosition());

                if (stat.getUnix() > heighestUnixTime) {
                    heighestUnixTime = stat.getUnix();
                }
            }

            leaderboard[index] = new String[]{gameStat.getPrettyStat(), score, position};
            index++;
        }

        BufferedImage skin;
        try {
            skin = skinFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            skin = null;
        }

        final String[] header = {playerStatsInfo.getName(), playerStatsInfo.getGame(), playerStatsInfo.getBoard()};
        final PictureTable statsPicture = new PictureTable(header, this.getFormattedUnixTime(heighestUnixTime), leaderboard, skin);
        final Optional<InputStream> picture = statsPicture.getPlayerPicture();
        if (picture.isPresent()) {
            commandParameters.getDiscordChannel().sendFile(picture.get(), String.join("-", header) + "-" + heighestUnixTime + ".png").queue();
            return CommandResult.SUCCESS;
        }

        this.sendErrorMessage(commandParameters, "Error while creating picture.");
        return CommandResult.ERROR;
    }
}
