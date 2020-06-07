package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.bedrock.BedrockGame;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class BedrockGamesCommand extends AbstractBedrockStatsCommand {
    public BedrockGamesCommand() {
        super("bgames", "Bedrock games", "", "bg");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final EmbedBuilder message = this.getEmbedBuilder(commandParameters)
                .setTitle("Bedrock Games");

        this.getStatsModule().getBedrockGames().values()
                .stream()
                .collect(Collectors.groupingBy(BedrockGame::isRemoved, TreeMap::new, Collectors.toList()))
                .forEach((key, value) ->
                        message.addField(
                                key ? "Removed" : "Games",
                                value.stream()
                                        .map(BedrockGame::getName)
                                        .sorted(Comparator.naturalOrder())
                                        .collect(Collectors.joining("\n ")),
                                false
                        ));

        this.sendTimedMessage(commandParameters, message, 150);
        return CommandResult.SUCCESS;
    }
}
