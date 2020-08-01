package de.timmi6790.external_modules.mineplexstats.commands.debug;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.utilities.EnumUtilities;
import de.timmi6790.external_modules.mineplexstats.MineplexStatsModule;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class ReloadDataCommand extends AbstractCommand {
    public ReloadDataCommand() {
        super("sReload", "Debug", "", "<javaGame|javaGroup|bedrockGame>", "sr");

        this.setMinArgs(1);
        this.setPermission("mineplexstats.debug.reload");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final MineplexStatsModule module = DiscordBot.getModuleManager().getModuleOrThrow(MineplexStatsModule.class);
        final ValidArgs0 arg0 = this.getFromEnumIgnoreCaseThrow(commandParameters, 0, ValidArgs0.values());

        switch (arg0) {
            case JAVA_GAME:
                module.loadJavaGames();
                break;

            case JAVA_GROUP:
                module.loadJavaGroups();
                break;

            case BEDROCK_GAME:
                module.loadBedrockGames();
                break;
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Reloaded data")
                        .setDescription("Reloaded " + MarkdownUtil.monospace(EnumUtilities.getPrettyName(arg0))),
                90
        );
        return CommandResult.SUCCESS;
    }

    private enum ValidArgs0 {
        JAVA_GAME,
        JAVA_GROUP,
        BEDROCK_GAME
    }
}
