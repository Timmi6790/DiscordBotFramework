package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.management;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.EmptyEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;
import de.timmi6790.statsbotdiscord.utilities.DiscordEmotes;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerFilterCommand extends AbstractJavaStatsCommand {
    public PlayerFilterCommand() {
        super("filter", "Filter Players", "<uuid> <game> <stat> <board>");

        this.setMinArgs(4);
        this.setPermission("mineplexstats.management.filter");
        this.addDiscordPermissions(Permission.MANAGE_EMOTES);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final UUID uuid = this.getUUID(commandParameters, 0);
        final JavaGame game = this.getGame(commandParameters, 1);
        final JavaStat stat = this.getStat(game, commandParameters, 2);
        final JavaBoard board = this.getBoard(game, stat, commandParameters, 3);

        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        final EmbedBuilder embedBuilder = UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                .addField("Player UUID", uuid.toString(), false)
                .addField("Game", game.getName(), false)
                .addField("Stat", stat.getName(), false)
                .addField("Board", board.getName(), false);

        emotes.put(DiscordEmotes.CHECK_MARK.getEmote(), new AbstractEmoteReaction() {
            @Override
            public void onEmote() {
                PlayerFilterCommand.this.getStatsModule().getMpStatsRestClient().addJavaPlayerFilter(uuid, game.getName(), stat.getName(), board.getName());

                PlayerFilterCommand.this.sendTimedMessage(
                        commandParameters,
                        embedBuilder.setTitle("Successfully Filtered"),
                        90
                );
            }
        });
        emotes.put(DiscordEmotes.RED_CROSS_MARK.getEmote(), new EmptyEmoteReaction());

        this.sendEmoteMessage(
                commandParameters,
                embedBuilder.setTitle("Filter Confirm")
                        .setDescription("Are you sure that you want to filter this person?"),
                emotes
        );

        return CommandResult.SUCCESS;
    }
}
