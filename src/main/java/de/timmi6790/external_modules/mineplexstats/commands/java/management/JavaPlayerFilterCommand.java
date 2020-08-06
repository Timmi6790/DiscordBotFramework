package de.timmi6790.external_modules.mineplexstats.commands.java.management;

import de.timmi6790.discord_framework.datatypes.MapBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.EmptyEmoteReaction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.external_modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaStat;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.util.LinkedHashMap;
import java.util.UUID;

public class JavaPlayerFilterCommand extends AbstractJavaStatsCommand {
    public JavaPlayerFilterCommand() {
        super("filter", "Filter Players", "<uuid> <game> <stat> <board>");

        this.setCategory("MineplexStats - Java - Management");
        this.addProperties(
                new MinArgCommandProperty(4),
                new RequiredDiscordBotPermsCommandProperty(
                        Permission.MESSAGE_ADD_REACTION
                )
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final UUID uuid = this.getUUID(commandParameters, 0);
        final JavaGame game = this.getGame(commandParameters, 1);
        final JavaStat stat = this.getStat(game, commandParameters, 2);
        final JavaBoard board = this.getBoard(game, stat, commandParameters, 3);

        final EmbedBuilder embedBuilder = this.getEmbedBuilder(commandParameters)
                .addField("Player UUID", uuid.toString(), false)
                .addField("Game", game.getName(), false)
                .addField("Stat", stat.getName(), false)
                .addField("Board", board.getName(), false);

        this.sendEmoteMessage(
                commandParameters,
                embedBuilder.setTitle("Filter Confirm")
                        .setDescription("Are you sure that you want to filter this person?"),
                new MapBuilder<String, AbstractEmoteReaction>(() -> new LinkedHashMap<>(2))
                        .put(DiscordEmotes.CHECK_MARK.getEmote(), new AbstractEmoteReaction() {
                            @Override
                            public void onEmote() {
                                JavaPlayerFilterCommand.this.getStatsModule().getMpStatsRestClient().addJavaPlayerFilter(uuid, game.getName(), stat.getName(), board.getName());

                                JavaPlayerFilterCommand.this.sendTimedMessage(
                                        commandParameters,
                                        embedBuilder.setTitle("Successfully Filtered"),
                                        90
                                );
                            }
                        })
                        .put(DiscordEmotes.RED_CROSS_MARK.getEmote(), new EmptyEmoteReaction())
                        .build()
        );

        return CommandResult.SUCCESS;
    }
}
