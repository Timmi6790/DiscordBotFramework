package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.PictureTable;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGroupsPlayer;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JavaPlayerGroupCommand extends AbstractJavaStatsCommand {
    public JavaPlayerGroupCommand() {
        super("gplayer", "MineplexStats - Java", "Group players", "<player> <group> <stat> [board] [date]");

        this.setMinArgs(3);
        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String player = this.getPlayer(commandParameters, 0);

        final MineplexStatsModule module = ((MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class));
        final ResponseModel responseModel = module.getMpStatsRestClient().getPlayerGroup(player, "MixedArcade", "Wins", "All");

        if (!(responseModel instanceof JavaGroupsPlayer)) {
            commandParameters.getEvent().getChannel().sendMessage(
                    UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                            .setTitle("No stats available")
                            .setDescription(this.getApiErrorMessage(responseModel))
                            .build())
                    .queue();
            return CommandResult.ERROR;
        }

        final JavaGroupsPlayer groupStats = (JavaGroupsPlayer) responseModel;
        final JavaGroupsPlayer.JavaGroupsPlayerInfo playerStatsInfo = ((JavaGroupsPlayer) responseModel).getInfo();

        final CompletableFuture<BufferedImage> skinFuture = this.getPlayerSkin(playerStatsInfo.getUuid());

        final String[][] leaderboard = new String[groupStats.getStats().size() + 1][3];
        leaderboard[0] = new String[]{"Game", "Score", "Position"};

        int heighestUnixTime = 0;
        int index = 1;
        for (final JavaGroupsPlayer.JavaGroupsPlayerStat stat : groupStats.getStats().values()) {
            leaderboard[index] = new String[]{stat.getGame(), String.valueOf(stat.getScore()), String.valueOf(stat.getPosition())};

            if (stat.getUnix() > heighestUnixTime) {
                heighestUnixTime = stat.getUnix();
            }

            index++;
        }

        BufferedImage skin;
        try {
            skin = skinFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            skin = null;
        }

        final String[] header = {playerStatsInfo.getName(), playerStatsInfo.getGroup(), playerStatsInfo.getPrettyStat(), playerStatsInfo.getBoard()};
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
