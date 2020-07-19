package de.timmi6790.external_modules.mineplexstats.commands.debug;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.external_modules.mineplexstats.MineplexStatsModule;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Arrays;
import java.util.List;

public class ReloadDataCommand extends AbstractCommand {
    private static final List<String> VALID_0_ARGS = Arrays.asList("javaGame", "javaGroup", "bedrockGame");

    public ReloadDataCommand() {
        super("sReload", "Debug", "", "[data]", "sr");

        this.setMinArgs(1);
        this.setPermission("mineplexstats.debug.reload");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final MineplexStatsModule module = DiscordBot.getModuleManager().getModule(MineplexStatsModule.class).orElseThrow(RuntimeException::new);
        final String arg0 = this.getFromListIgnoreCase(commandParameters, 0, VALID_0_ARGS);

        switch (arg0) {
            case "javaGame":
                module.loadJavaGames();
                break;

            case "javaGroup":
                module.loadJavaGroups();
                break;

            case "bedrockGame":
                module.loadBedrockGames();
                break;
            default:
                return CommandResult.ERROR;
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Reloaded data")
                        .setDescription("Reloaded " + MarkdownUtil.monospace(arg0)),
                90
        );
        return CommandResult.SUCCESS;
    }
}
