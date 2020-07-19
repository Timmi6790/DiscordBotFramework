package de.timmi6790.external_modules.mineplexstats.commands.java.management;

import de.timmi6790.discord_framework.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.external_modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.apache.commons.lang3.ArrayUtils;

public class JavaBoardAliasCommand extends AbstractJavaStatsCommand {
    private static final String[] JAVA_BOARDS = {"all", "daily", "weekly", "monthly", "yearly"};

    public JavaBoardAliasCommand() {
        super("aliasBoard", "Board Alias", "<board> <alias>", "ab");

        this.setCategory("MineplexStats - Java - Management");
        this.setPermission("mineplexstats.management.aliasBoard");
        this.setMinArgs(2);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        if (!ArrayUtils.contains(JAVA_BOARDS, commandParameters.getArgs()[0])) {
            throw new CommandReturnException(
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Invalid Board")
                            .addField("Boards", String.join(", ", JAVA_BOARDS), false)
            );
        }

        this.getStatsModule().getMpStatsRestClient().addJavaBoardAlias(commandParameters.getArgs()[0], commandParameters.getArgs()[1]);
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
