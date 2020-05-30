package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;

import java.util.Comparator;
import java.util.stream.Collectors;

public class BedrockGamesCommand extends AbstractBedrockStatsCommand {
    public BedrockGamesCommand() {
        super("bgames", "Bedrock games", "", "bg");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Bedrock Games")
                        .setDescription(this.getStatsModule().getBedrockGames().values()
                                .stream()
                                .sorted(Comparator.naturalOrder())
                                .collect(Collectors.joining("\n"))),
                150
        );
        return CommandResult.SUCCESS;
    }
}
