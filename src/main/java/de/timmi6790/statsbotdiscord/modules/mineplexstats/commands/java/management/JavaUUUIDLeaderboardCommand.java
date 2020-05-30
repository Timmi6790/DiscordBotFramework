package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.management;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.datatypes.ListBuilder;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.EmoteReactionMessage;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.CommandEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.PictureTable;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaLeaderboard;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;
import de.timmi6790.statsbotdiscord.utilities.DiscordEmotes;
import net.dv8tion.jda.api.Permission;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaUUUIDLeaderboardCommand extends AbstractJavaStatsCommand {
    private final static int ARG_POS_BOARD_POS = 2;
    private final static int ARG_POS_START_POS = 3;
    private final static int ARG_POS_END_POS = 4;

    private final static int LEADERBOARD_UPPER_LIMIT = 1_000;

    public JavaUUUIDLeaderboardCommand() {
        super("uuidLeaderboard", "Java UUID Leaderboard", "<game> <stat> [board] [start] [end] [date]", "ul");

        this.setMinArgs(2);
        this.setPermission("mineplexstats.management.filter");
        this.addDiscordPermissions(Permission.MESSAGE_ADD_REACTION);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaGame game = this.getGame(commandParameters, 0);
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        final JavaBoard board = this.getBoard(game, commandParameters, ARG_POS_BOARD_POS);
        final int startPos = this.getStartPosition(commandParameters, ARG_POS_START_POS, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPosition(startPos, commandParameters, ARG_POS_END_POS, LEADERBOARD_UPPER_LIMIT, 5);
        final long unixTime = this.getUnixTime(commandParameters, 5);

        final MineplexStatsModule module = this.getStatsModule();
        final ResponseModel responseModel = module.getMpStatsRestClient().getJavaLeaderboard(game.getName(), stat.getName(), board.getName(), startPos, endPos, unixTime);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        final JavaLeaderboard leaderboardResponse = (JavaLeaderboard) responseModel;
        final JavaLeaderboard.Info leaderboardInfo = leaderboardResponse.getInfo();

        final AtomicInteger index = new AtomicInteger(1);
        final String[][] leaderboard = new ListBuilder<String[]>(() -> new ArrayList<>(leaderboardResponse.getLeaderboard().size() + 1))
                .add(new String[]{"Emote", "UUID", "Player", "Score", "Position"})
                .addAll(leaderboardResponse.getLeaderboard()
                        .stream()
                        .map(data -> new String[]{String.valueOf(index.getAndIncrement()), data.getUuid().toString(), data.getName(),
                                this.getFormattedScore(stat, data.getScore()), String.valueOf(data.getPosition())}))
                .build()
                .toArray(new String[0][3]);

        final String[] header = {leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard()};
        final Optional<InputStream> picture = new PictureTable(header, this.getFormattedUnixTime(leaderboardInfo.getUnix()), leaderboard).getPlayerPicture();
        if (picture.isPresent()) {
            final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

            StatsBot.getCommandManager().getCommand(JavaPlayerFilterCommand.class).ifPresent(filterCommand -> {
                final AtomicInteger emoteIndex = new AtomicInteger(1);
                leaderboardResponse.getLeaderboard().forEach(data -> {
                    final CommandParameters newParameters = new CommandParameters(commandParameters);
                    newParameters.setArgs(new String[]{data.getUuid().toString(), leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard()});
                    emotes.put(DiscordEmotes.getNumberEmote(emoteIndex.getAndIncrement()).getEmote(), new CommandEmoteReaction(filterCommand, newParameters));
                });
            });

            // Leaderboard default responses
            final int rowDistance = endPos - startPos;
            final int fastRowDistance = leaderboardInfo.getTotalLength() * 10 / 100;

            // Create a new args array if the old array has no positions
            if (Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1 > commandParameters.getArgs().length) {
                final String[] newArgs = new String[Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1];
                newArgs[ARG_POS_BOARD_POS] = board.getName();

                System.arraycopy(commandParameters.getArgs(), 0, newArgs, 0, commandParameters.getArgs().length);
                commandParameters.setArgs(newArgs);
            }

            // Far Left Arrow
            if (startPos - rowDistance > 2) {
                final int newStart = Math.max(1, (startPos - fastRowDistance));
                this.addMessageEmote(commandParameters, emotes, DiscordEmotes.FAR_LEFT_ARROW, newStart, rowDistance);
            }

            // Left Arrow
            if (startPos > 1) {
                final int newStart = Math.max(1, (startPos - rowDistance - 1));
                this.addMessageEmote(commandParameters, emotes, DiscordEmotes.LEFT_ARROW, newStart, rowDistance);
            }

            // Right Arrow
            if (leaderboardInfo.getTotalLength() > endPos) {
                final int newStart = Math.min(leaderboardInfo.getTotalLength(), (endPos + rowDistance + 1)) - rowDistance;
                this.addMessageEmote(commandParameters, emotes, DiscordEmotes.RIGHT_ARROW, newStart, rowDistance);
            }

            // Far Right Arrow
            if (leaderboardInfo.getTotalLength() - rowDistance - 1 > endPos) {
                final int newStart = Math.min(leaderboardInfo.getTotalLength(), (endPos + fastRowDistance)) - rowDistance;
                this.addMessageEmote(commandParameters, emotes, DiscordEmotes.FAR_RIGHT_ARROW, newStart, rowDistance);
            }

            final EmoteReactionMessage emoteReactionMessage = new EmoteReactionMessage(emotes, commandParameters.getEvent().getAuthor().getIdLong(), commandParameters.getEvent().getChannel().getIdLong());
            commandParameters.getDiscordChannel()
                    .sendFile(picture.get(), String.join("-", header) + "-" + leaderboardInfo.getUnix() + ".png")
                    .queue(message -> StatsBot.getEmoteReactionManager().addEmoteReactionMessage(message, emoteReactionMessage));
            return CommandResult.SUCCESS;
        }

        this.sendErrorMessage(commandParameters, "Error while creating picture.");
        return CommandResult.ERROR;
    }

    private void addMessageEmote(final CommandParameters commandParameters, final Map<String, AbstractEmoteReaction> emotes, final DiscordEmotes emote, final int newStart, final int rowDistance) {
        final CommandParameters newParameters = new CommandParameters(commandParameters);

        newParameters.getArgs()[ARG_POS_START_POS] = String.valueOf(newStart);
        newParameters.getArgs()[ARG_POS_END_POS] = String.valueOf(newStart + rowDistance);

        emotes.put(emote.getEmote(), new CommandEmoteReaction(this, newParameters));
    }
}
