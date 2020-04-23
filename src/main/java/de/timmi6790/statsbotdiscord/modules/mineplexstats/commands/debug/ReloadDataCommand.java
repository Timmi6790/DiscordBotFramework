package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.debug;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class ReloadDataCommand extends AbstractCommand {
    public ReloadDataCommand() {
        super("sReload", "Debug", "", "[data]", "sr");

        this.setMinArgs(1);
        this.setPermission("mineplexstats.debug.reload");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final MineplexStatsModule module = (MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class);

        final String data = commandParameters.getArgs()[0];
        switch (data.toLowerCase()) {
            case "javagame":
                module.loadJavaGames();
                break;

            case "javagroup":
                module.loadJavaGroups();
                break;

            case "bedrockgame":
                module.loadBedrockGames();
                break;

            default:
                throw new CommandReturnException(
                        UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                                .setTitle("Incorrect data type")
                                .setDescription(MarkdownUtil.monospace(data) + " is not a valid type.\n" +
                                        "[javaGame, javaGroup, bedrockGame]")
                );
        }

        this.sendSuccessMessage(commandParameters, data);
        return CommandResult.SUCCESS;
    }

    private void sendSuccessMessage(final CommandParameters commandParameters, final String data) {
        throw new CommandReturnException(
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Reloaded data")
                        .setDescription("Reloaded " + MarkdownUtil.monospace(data)),
                CommandResult.SUCCESS
        );
    }
}
