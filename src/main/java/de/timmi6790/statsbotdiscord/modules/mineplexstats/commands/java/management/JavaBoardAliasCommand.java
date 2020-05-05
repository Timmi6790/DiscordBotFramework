package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.management;

import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.apache.commons.lang3.ArrayUtils;

public class JavaBoardAliasCommand extends AbstractJavaStatsCommand {
    private final static String[] JAVA_BOARDS = {"all", "daily", "weekly", "monthly", "yearly"};

    public JavaBoardAliasCommand() {
        super("aliasBoard", "Board Alias", "<board> <alias>", "ab");

        this.setPermission("mineplexstats.management.aliasBoard");
        this.setMinArgs(2);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        if (!ArrayUtils.contains(JAVA_BOARDS, commandParameters.getArgs()[0])) {
            throw new CommandReturnException(
                    UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                            .setTitle("Invalid Board")
                            .addField("Boards", String.join(", ", JAVA_BOARDS), false)
            );
        }

        this.getStatsModule().getMpStatsRestClient().addJavaBoardAlias(commandParameters.getArgs()[0], commandParameters.getArgs()[1]);
        this.getStatsModule().loadJavaGames();
        this.sendTimedMessage(
                commandParameters,
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Added Board Alias")
                        .setDescription("Added new board alias " + MarkdownUtil.monospace(commandParameters.getArgs()[0])),
                90
        );

        return CommandResult.SUCCESS;
    }
}
