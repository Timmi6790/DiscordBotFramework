package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.JavaStat;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.PictureTable;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.errors.ErrorModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.PlayerStats;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import net.dv8tion.jda.api.Permission;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


public class JavaPlayerStatsCommand extends AbstractJavaStatsCommand {
    public JavaPlayerStatsCommand() {
        super("player", "MineplexStats - Java", "Player stats", "<player> <game> [board] [date]", "pl");

        this.addDiscordPermission(Permission.MESSAGE_ATTACH_FILES);
        this.setMinArgs(2);
        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaGame javaGame = this.getGame(commandParameters, 1);

        final ResponseModel responseModel = ((MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class)).getMpStatsRestClient().getPlayerStats(commandParameters.getArgs()[0], javaGame.getName(), "all");

        if (!(responseModel instanceof PlayerStats)) {
            if (responseModel instanceof ErrorModel) {
                commandParameters.getEvent().getChannel().sendMessage(UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters).setTitle("Error").setDescription(((ErrorModel) responseModel).getErrorMessage()).build()).queue();
            }
            System.out.println("Error");
            return CommandResult.ERROR;
        }

        final PlayerStats playerStats = (PlayerStats) responseModel;
        final PlayerStats.PlayerStatsInfo playerStatsInfo = playerStats.getInfo();

        final CompletableFuture<BufferedImage> skin = this.getPlayerSkin(playerStatsInfo.getUuid());

        final JavaGame game = ((MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class)).getJavaGame(playerStatsInfo.getGame()).get();
        final Map<String, PlayerStats.PlayerStatsStats> stats = playerStats.getStats();

        int heighestUnixTime = 0;
        final String[][] leaderboard = new String[game.getStats().size() + 1][3];
        leaderboard[0] = new String[]{"Category", "Score", "Position"};

        final Map<String, JavaStat> gameStats = game.getStats();
        int index = 1;
        for (final String statName : game.getStatNames()) {
            final JavaStat gameStat = gameStats.get(statName.toLowerCase());
            String score = "Unknown";
            String position = ">1000";
            if (stats.containsKey(gameStat.getName())) {
                final PlayerStats.PlayerStatsStats stat = stats.get(gameStat.getName());

                score = this.getFormattedScore(gameStat, stat.getScore());
                position = String.valueOf(stat.getPosition());

                if (stat.getUnix() > heighestUnixTime) {
                    heighestUnixTime = stat.getUnix();
                }
            }

            leaderboard[index] = new String[]{gameStat.getPrettyStat(), score, position};
            index++;
        }

        final String[] header = {playerStatsInfo.getName(), playerStatsInfo.getGame(), playerStatsInfo.getBoard()};

        final PictureTable statsPicture;
        try {
            statsPicture = new PictureTable(header, this.getFormattedUnixTime(heighestUnixTime), leaderboard, skin.get());
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
            commandParameters.getDiscordChannel().sendMessage("Error").queue();
            return CommandResult.ERROR;
        }

        final Optional<InputStream> picture = statsPicture.getPlayerPicture();
        if (picture.isPresent()) {
            commandParameters.getDiscordChannel().sendFile(picture.get(), String.join("-", header) + "-" + heighestUnixTime + ".png").queue();
            return CommandResult.SUCCESS;
        }

        commandParameters.getDiscordChannel().sendMessage("Error").queue();
        return CommandResult.ERROR;

    }

    public CompletableFuture<BufferedImage> getPlayerSkin(final UUID uuid) {
        final CompletableFuture<BufferedImage> completableFuture = new CompletableFuture<>();
        Executors.newSingleThreadExecutor().submit(() -> {
            final HttpResponse<byte[]> response = Unirest.get("https://visage.surgeplay.com/frontfull/" + uuid.toString().replace("-", "") + ".png")
                    .connectTimeout(6_000)
                    .asBytes();

            if (!response.isSuccess()) {
                completableFuture.complete(null);
                return;
            }

            try {
                final InputStream in = new ByteArrayInputStream(response.getBody());
                completableFuture.complete(ImageIO.read(in));
                return;
            } catch (final IOException e) {
                e.printStackTrace();
            }

            completableFuture.complete(null);
        });

        return completableFuture;
    }
}
