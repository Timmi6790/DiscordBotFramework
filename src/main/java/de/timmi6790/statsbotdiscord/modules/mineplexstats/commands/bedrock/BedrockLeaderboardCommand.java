package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.bedrock;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.datatypes.ListBuilder;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.EmoteReactionMessage;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.CommandEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.PictureTable;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.bedrock.BedrockLeaderboard;
import de.timmi6790.statsbotdiscord.utilities.DiscordEmotes;

import java.io.InputStream;
import java.util.*;

public class BedrockLeaderboardCommand extends AbstractBedrockStatsCommand {
    private final static int ARG_POS_START_POS = 1;
    private final static int ARG_POS_END_POS = 2;

    private final static int LEADERBOARD_UPPER_LIMIT = 100;

    public BedrockLeaderboardCommand() {
        super("bleaderboard", "Bedrock Leaderboard", "<game> [start] [end] [date]", "bl", "blb");

        this.setDefaultPerms(true);
        this.setMinArgs(1);
    }

    private void addMessageEmote(final CommandParameters commandParameters, final Map<String, AbstractEmoteReaction> emotes, final DiscordEmotes emote, final int newStart, final int rowDistance) {
        final CommandParameters newParameters = new CommandParameters(commandParameters);

        newParameters.getArgs()[ARG_POS_START_POS] = String.valueOf(newStart);
        newParameters.getArgs()[ARG_POS_END_POS] = String.valueOf(newStart + rowDistance);

        emotes.put(emote.getEmote(), new CommandEmoteReaction(this, newParameters));
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final String game = this.getGame(commandParameters, 0);
        final int startPos = this.getStartPosition(commandParameters, ARG_POS_START_POS, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPosition(startPos, commandParameters, ARG_POS_END_POS, LEADERBOARD_UPPER_LIMIT);
        final long unixTime = this.getUnixTime(commandParameters, 3);

        final ResponseModel responseModel = this.getStatsModule().getMpStatsRestClient().getBedrockLeaderboard(game, startPos, endPos, unixTime);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        // Parse the data into the image maker format
        final List<BedrockLeaderboard.Leaderboard> leaderboardResponse = ((BedrockLeaderboard) responseModel).getLeaderboard();
        final String[][] leaderboard = new ListBuilder<String[]>(() -> new ArrayList<>(leaderboardResponse.size() + 1))
                .add(new String[]{"Player", "Score", "Position"})
                .addAll(leaderboardResponse.stream()
                        .map(data -> new String[]{data.getName(), this.getFormattedNumber(data.getScore()), String.valueOf(data.getPosition())}))
                .build()
                .toArray(new String[0][3]);

        final BedrockLeaderboard.Info leaderboardInfo = ((BedrockLeaderboard) responseModel).getInfo();

        final String[] header = {"Bedrock " + leaderboardInfo.getGame()};
        final Optional<InputStream> picture = new PictureTable(header, this.getFormattedUnixTime(leaderboardInfo.getUnix()), leaderboard).getPlayerPicture();
        if (picture.isPresent()) {
            final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

            final int rowDistance = endPos - startPos;
            final int fastRowDistance = leaderboardInfo.getTotalLength() * 50 / 100;

            if (Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1 > commandParameters.getArgs().length) {
                final String[] newArgs = new String[Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1];
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

            commandParameters.getDiscordChannel()
                    .sendFile(picture.get(), String.join("-", header) + "-" + leaderboardInfo.getUnix() + ".png")
                    .queue(message ->
                            StatsBot.getEmoteReactionManager().addEmoteReactionMessage(
                                    message,
                                    new EmoteReactionMessage(emotes, commandParameters.getEvent().getAuthor().getIdLong(), commandParameters.getEvent().getChannel().getIdLong())
                            )
                    );
            return CommandResult.SUCCESS;
        }

        this.sendErrorMessage(commandParameters, "Error while creating picture.");
        return CommandResult.ERROR;
    }
}

