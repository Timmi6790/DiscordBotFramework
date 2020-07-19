package de.timmi6790.external_modules.mineplexstats.commands.java.management;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.external_modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaStat;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class JavaStatAliasCommand extends AbstractJavaStatsCommand {
    public JavaStatAliasCommand() {
        super("aliasStat", "Stat Alias", "<game> <stat> <alias>", "as");

        this.setCategory("MineplexStats - Java - Management");
        this.setPermission("mineplexstats.management.aliasStat");
        this.setMinArgs(3);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaGame game = this.getGame(commandParameters, 0);
        final JavaStat stat = this.getStat(game, commandParameters, 1);

        this.getStatsModule().getMpStatsRestClient().addJavaStatAlias(game.getName(), stat.getName(), commandParameters.getArgs()[2]);
        this.getStatsModule().loadJavaGames();
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Added Stat Alias")
                        .setDescription("Added new stat alias " + MarkdownUtil.monospace(commandParameters.getArgs()[2]) + " for " +
                                MarkdownUtil.bold(game.getName() + " " + stat.getPrintName())),
                90
        );

        return CommandResult.SUCCESS;
    }
}
