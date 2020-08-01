package de.timmi6790.external_modules.mineplexstats.commands.bedrock.management;

import de.timmi6790.discord_framework.datatypes.ListBuilder;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.external_modules.mineplexstats.commands.bedrock.AbstractBedrockStatsCommand;
import de.timmi6790.external_modules.mineplexstats.picture.PictureTable;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock.BedrockGame;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.bedrock.BedrockLeaderboard;
import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BedrockFilterLeaderboardCommand extends AbstractBedrockStatsCommand {
    private static final int ARG_POS_START_POS = 1;
    private static final int ARG_POS_END_POS = 2;

    private static final int LEADERBOARD_UPPER_LIMIT = 100;

    public BedrockFilterLeaderboardCommand() {
        super("bfleaderboard", "Bedrock Filter Leaderboard", "<game> [start] [end] [date]", "bfl", "bflb");

        this.setPermission("mineplexstats.management.bfilter");
        this.setMinArgs(1);
        this.addDiscordPermissions(Permission.MESSAGE_ADD_REACTION);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final BedrockGame game = this.getGame(commandParameters, 0);
        final int startPos = this.getStartPosition(commandParameters, ARG_POS_START_POS, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPosition(startPos, commandParameters, ARG_POS_END_POS, LEADERBOARD_UPPER_LIMIT);
        final long unixTime = this.getUnixTime(commandParameters, 3);

        final ResponseModel responseModel = this.getStatsModule().getMpStatsRestClient().getBedrockLeaderboard(game.getName(), startPos, endPos, unixTime);
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

        // Emotes
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).getCommand(BedrockPlayerFilterCommand.class).ifPresent(filterCommand -> {
            final AtomicInteger emoteIndex = new AtomicInteger(1);
            leaderboardResponse.forEach(data -> {
                final CommandParameters newParameters = new CommandParameters(commandParameters);
                newParameters.setArgs(new String[]{leaderboardInfo.getGame(), data.getName()});
                emotes.put(DiscordEmotes.getNumberEmote(emoteIndex.getAndIncrement()).getEmote(), new CommandEmoteReaction(filterCommand, newParameters));
            });
        });

        // Emotes
        final int rowDistance = endPos - startPos;
        final int fastRowDistance = leaderboardInfo.getTotalLength() * 50 / 100;

        if (Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1 > commandParameters.getArgs().length) {
            final String[] newArgs = new String[Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1];
            System.arraycopy(commandParameters.getArgs(), 0, newArgs, 0, commandParameters.getArgs().length);
            commandParameters.setArgs(newArgs);
        }

        emotes.putAll(
                this.getLeaderboardEmotes(
                        commandParameters,
                        rowDistance,
                        fastRowDistance,
                        startPos,
                        endPos,
                        leaderboardInfo.getTotalLength(),
                        ARG_POS_START_POS,
                        ARG_POS_END_POS
                )
        );

        return this.sendPicture(
                commandParameters,
                new PictureTable(header, this.getFormattedUnixTime(leaderboardInfo.getUnix()), leaderboard).getPlayerPicture(),
                String.join("-", header) + "-" + leaderboardInfo.getUnix(),
                new EmoteReactionMessage(
                        emotes,
                        commandParameters.getEvent().getAuthor().getIdLong(),
                        commandParameters.getEvent().getChannel().getIdLong()
                )
        );
    }
}

