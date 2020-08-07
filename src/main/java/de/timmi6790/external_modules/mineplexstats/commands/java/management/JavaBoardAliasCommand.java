package de.timmi6790.external_modules.mineplexstats.commands.java.management;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.external_modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Arrays;

public class JavaBoardAliasCommand extends AbstractJavaStatsCommand {
    private static final String[] JAVA_BOARDS = {"all", "daily", "weekly", "monthly", "yearly"};

    public JavaBoardAliasCommand() {
        super("aliasBoard", "Board Alias", "<board> <alias>", "ab");

        this.setCategory("MineplexStats - Java - Management");
        this.addProperties(
                new MinArgCommandProperty(2)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String board = this.getFromListIgnoreCaseThrow(commandParameters, 0, Arrays.asList(JAVA_BOARDS));
        this.getStatsModule().getMpStatsRestClient().addJavaBoardAlias(board, commandParameters.getArgs()[1]);
        this.getStatsModule().loadJavaGames();
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Added Board Alias")
                        .setDescription("Added new board alias " + MarkdownUtil.monospace(commandParameters.getArgs()[0])),
                90
        );

        return CommandResult.SUCCESS;
    }
}
