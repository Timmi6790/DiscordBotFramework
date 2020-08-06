package de.timmi6790.external_modules.mineplexstats.commands.java.management;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.external_modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGame;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class JavaGameAliasCommand extends AbstractJavaStatsCommand {
    public JavaGameAliasCommand() {
        super("aliasGame", "Game Alias", "<game> <alias>", "ag");

        this.setCategory("MineplexStats - Java - Management");
        this.addProperties(
                new MinArgCommandProperty(2)
        );
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
                        .setDescription("Added new game alias " + MarkdownUtil.monospace(commandParameters.getArgs()[0])),
                90
        );

        return CommandResult.SUCCESS;
    }
}
