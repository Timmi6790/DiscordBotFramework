package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class BedrockGamesCommand extends AbstractBedrockStatsCommand {
    public BedrockGamesCommand() {
        super("bgames", "Bedrock Games", "", "bg");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final List<String> games = new ArrayList<>(this.getStatsModule().getBedrockGames().values());
        games.sort(Comparator.naturalOrder());

        final StringJoiner description = new StringJoiner("\n");
        for (final String game : games) {
            description.add(game);
        }

        commandParameters.getDiscordChannel().sendMessage(
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Bedrock Games")
                        .setDescription(description.toString())
                        .build())
                .delay(150, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue();

        return CommandResult.SUCCESS;
    }
}
