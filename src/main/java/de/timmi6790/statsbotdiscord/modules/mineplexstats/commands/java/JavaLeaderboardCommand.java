package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;

public class JavaLeaderboardCommand extends AbstractJavaStatsCommand {
    public JavaLeaderboardCommand() {
        super("leaderboard", "leaderboards", "<game> <stat> [board] [start] [end] [date]", "lb");

        this.setDefaultPerms(true);
        this.setMinArgs(2);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaGame game = this.getGame(commandParameters, 0);
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        final JavaBoard board = this.getBoard(game, commandParameters, 2);

        final MineplexStatsModule module = this.getStatsModule();
        // final ResponseModel responseModel = module.getMpStatsRestClient().getJavaPlayerStats(player, game.getName(), board.getName());
        // this.checkApiResponse(commandParameters, responseModel, "No stats available");

        return null;
    }
}
