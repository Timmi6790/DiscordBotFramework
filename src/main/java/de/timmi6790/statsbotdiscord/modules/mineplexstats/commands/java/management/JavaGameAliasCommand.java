package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.management;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class JavaGameAliasCommand extends AbstractJavaStatsCommand {
    public JavaGameAliasCommand() {
        super("aliasGame", "Game Alias", "<game> <alias>", "ag");

        this.setPermission("mineplexstats.management.aliasGame");
        this.setMinArgs(2);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaGame game = this.getGame(commandParameters, 0);
        this.getStatsModule().getMpStatsRestClient().addJavaGameAlias(game.getName(), commandParameters.getArgs()[1]);
        this.getStatsModule().loadJavaGames();
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Added Game Alias")
                        .setDescription("Added new board alias " + MarkdownUtil.monospace(commandParameters.getArgs()[0])),
                90
        );

        return CommandResult.SUCCESS;
    }
}
