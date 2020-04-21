package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.CommandEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.AbstractStatsCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGame;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGroup;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaStat;
import de.timmi6790.statsbotdiscord.utilities.DiscordEmotes;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesString;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public abstract class AbstractJavaStatsCommand extends AbstractStatsCommand {
    private final static Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");
    private final static List<String> STATS_TIME = new ArrayList<>(Arrays.asList("TimeInGame", "TimeInHub", "TimePlaying"));
    private final static DecimalFormat FORMAT_NUMBER = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private final static Cache<UUID, BufferedImage> SKIN_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    static {
        final DecimalFormatSymbols symbols = FORMAT_NUMBER.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        FORMAT_NUMBER.setDecimalFormatSymbols(symbols);
    }

    public AbstractJavaStatsCommand(final String name, final String description, final String syntax, final String... aliasNames) {
        super(name, "MineplexStats - Java", description, syntax, aliasNames);
    }

    protected String getFormattedScore(final JavaStat stat, final long score) {
        if (STATS_TIME.contains(stat.getName())) {
            return this.getFormattedTime(score);
        }

        return FORMAT_NUMBER.format(score);
    }

    private void sendHelpMessage(final CommandParameters commandParameters, final String userArg, final int argPos, final String argName,
                                 final AbstractCommand command, final String[] newArgs, final String[] similarNames) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder();
        description.append(MarkdownUtil.monospace(userArg)).append(" is not a valid ").append(argName).append(".\n");

        if (similarNames.length == 0) {
            description.append("Use the ").append(MarkdownUtil.bold(StatsBot.getCommandManager().getMainCommand() + " " + command.getName() + " " + String.join(" ", newArgs)))
                    .append(" command or click the ").append(DiscordEmotes.FOLDER.getEmote()).append(" emote to see all ").append(argName).append("s.");

        } else {
            description.append("Is it possible that you wanted to write?\n\n");

            for (int index = 0; similarNames.length > index; index++) {
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                description.append(emote).append(" ").append(MarkdownUtil.bold(similarNames[index])).append("\n");

                final CommandParameters newCommandParameters = commandParameters.clone();
                newCommandParameters.getArgs()[argPos] = similarNames[index];

                emotes.put(emote, new CommandEmoteReaction(this, newCommandParameters));
            }

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(MarkdownUtil.bold("All " + argName + "s"));
        }

        final CommandParameters newCommandParameters = commandParameters.clone();
        newCommandParameters.setArgs(newArgs);
        emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(command, newCommandParameters));

        this.sendEmoteMessage(commandParameters, "Invalid " + UtilitiesString.capitalize(argName), description.toString(), emotes);
    }

    protected JavaGame getGame(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        final Optional<JavaGame> game = this.getStatsModule().getJavaGame(name);
        if (game.isPresent()) {
            return game.get();
        }

        final List<String> similarNames = new ArrayList<>();
        for (final JavaGame similarGame : this.getStatsModule().getSimilarGames(name, 0.6, 3)) {
            similarNames.add(similarGame.getName());
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "game", command, new String[0], similarNames.toArray(new String[0]));

        throw new CommandReturnException();
    }

    protected JavaStat getStat(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaStat> stat = game.getStat(name);
        if (stat.isPresent()) {
            return stat.get();
        }

        final List<String> similarNames = new ArrayList<>();
        for (final JavaStat similarStat : game.getSimilarStats(name, 0.6, 3).toArray(new JavaStat[0])) {
            similarNames.add(similarStat.getName());
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "stat", command, new String[]{game.getName()}, similarNames.toArray(new String[0]));

        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name;
        if (argPos >= commandParameters.getArgs().length) {
            name = "All";
        } else {
            name = commandParameters.getArgs()[argPos];
        }

        for (final JavaStat stat : game.getStats().values()) {
            if (stat.getBoard(name).isPresent()) {
                return stat.getBoard(name).get();
            }
        }

        final Set<String> boards = new HashSet<>();
        JavaStat exampleStat = null;
        for (final JavaStat stat : game.getStats().values()) {
            boards.addAll(stat.getBoardNames());
            exampleStat = stat;
        }

        final List<String> similarBoards = UtilitiesData.getSimilarityList(name, boards, 0.6);
        final String[] similarNames = new String[Math.min(similarBoards.size(), 3)];
        for (int index = 0; similarNames.length > index; index++) {
            similarNames[index] = similarBoards.get(index);
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "board", command, new String[]{game.getName(), exampleStat != null ? exampleStat.getName() : ""}, similarNames);

        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final JavaStat stat, final CommandParameters commandParameters, final int argPos) {
        final String name;
        if (argPos >= commandParameters.getArgs().length) {
            name = "All";
        } else {
            name = commandParameters.getArgs()[argPos];
        }
        final Optional<JavaBoard> board = stat.getBoard(name);
        if (board.isPresent()) {
            return board.get();
        }

        final List<String> similarNames = new ArrayList<>();
        for (final JavaBoard similar : stat.getSimilarBoard(name, 0.6, 3).toArray(new JavaBoard[0])) {
            similarNames.add(similar.getName());
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "board", command, new String[]{game.getName(), stat.getName()}, similarNames.toArray(new String[0]));

        throw new CommandReturnException();
    }

    protected String getPlayer(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        if (NAME_PATTERN.matcher(name).find()) {
            return name;
        }

        throw new CommandReturnException(
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Invalid Name")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a minecraft name.")
        );
    }

    public JavaGroup getJavaGroup(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        final Optional<JavaGroup> group = this.getStatsModule().getJavaGroup(name);
        if (group.isPresent()) {
            return group.get();
        }

        final List<String> similarNames = new ArrayList<>();
        for (final JavaGroup similar : this.getStatsModule().getSimilarGroups(name, 0.6, 3).toArray(new JavaGroup[0])) {
            similarNames.add(similar.getName());
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "group", command, new String[]{}, similarNames.toArray(new String[0]));

        throw new CommandReturnException();
    }

    public JavaStat getJavaStat(final JavaGroup group, final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        for (final JavaGame game : group.getGames()) {
            if (game.getStat(name).isPresent()) {
                return game.getStat(name).get();
            }
        }

        final Set<String> statNames = new HashSet<>();
        for (final JavaStat stat : group.getStats()) {
            statNames.add(stat.getName());
        }

        final List<String> similarBoards = UtilitiesData.getSimilarityList(name, statNames, 0.6);
        final String[] similarNames = new String[Math.min(similarBoards.size(), 3)];
        for (int index = 0; similarNames.length > index; index++) {
            similarNames[index] = similarBoards.get(index);
        }

        final AbstractCommand command = StatsBot.getCommandManager().getCommand(JavaGroupsGroupsCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "stat", command, new String[]{group.getName()}, similarNames);

        throw new CommandReturnException();
    }

    public CompletableFuture<BufferedImage> getPlayerSkin(final UUID uuid) {
        final CompletableFuture<BufferedImage> completableFuture = new CompletableFuture<>();

        Executors.newSingleThreadExecutor().submit(() -> {
            final BufferedImage skin = SKIN_CACHE.getIfPresent(uuid);
            if (skin != null) {
                completableFuture.complete(skin);
                return;
            }

            final HttpResponse<byte[]> response = Unirest.get("https://visage.surgeplay.com/frontfull/" + uuid.toString().replace("-", "") + ".png")
                    .connectTimeout(6_000)
                    .asBytes();

            if (!response.isSuccess()) {
                completableFuture.complete(null);
                return;
            }

            try {
                final InputStream in = new ByteArrayInputStream(response.getBody());

                final BufferedImage image = ImageIO.read(in);
                SKIN_CACHE.put(uuid, image);

                completableFuture.complete(image);
                return;
            } catch (final IOException e) {
                e.printStackTrace();
            }

            completableFuture.complete(null);
        });

        return completableFuture;
    }
}
