package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.AbstractEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions.CommandEmoteReaction;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.errors.ErrorModel;
import de.timmi6790.statsbotdiscord.utilities.DiscordEmotes;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesString;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class AbstractStatsCommand extends AbstractCommand {
    protected final static String UNKNOWN_POSITION = "Unknown";
    protected final static String UNKNOWN_SCORE = ">1000";

    private final static int MAX_LEADERBOARD_POSITION_DISTANCE = 15;

    private final static DecimalFormat FORMAT_NUMBER = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private final static SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
    private final static DecimalFormat FORMAT_DECIMAL_POINT = new DecimalFormat(".##");

    static {
        final DecimalFormatSymbols date_symbol = FORMAT_DECIMAL_POINT.getDecimalFormatSymbols();
        date_symbol.setDecimalSeparator('.');
        FORMAT_DECIMAL_POINT.setDecimalFormatSymbols(date_symbol);

        final DecimalFormatSymbols number_symbol = FORMAT_NUMBER.getDecimalFormatSymbols();
        number_symbol.setGroupingSeparator(',');
        FORMAT_NUMBER.setDecimalFormatSymbols(number_symbol);

        FORMAT_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public AbstractStatsCommand(final String name, final String category, final String description, final String syntax, final String... aliasNames) {
        super(name, category, description, syntax, aliasNames);
    }

    protected MineplexStatsModule getStatsModule() {
        return (MineplexStatsModule) StatsBot.getModuleManager().getModule(MineplexStatsModule.class);
    }

    protected String getFormattedTime(long time) {
        final long days = TimeUnit.SECONDS.toDays(time);
        time -= TimeUnit.DAYS.toSeconds(days);

        final long hours = TimeUnit.SECONDS.toHours(time);
        time -= TimeUnit.HOURS.toSeconds(hours);

        if (days != 0) {
            if (hours == 0) {
                return days + (days > 1 ? " days" : " day");
            }

            return days + FORMAT_DECIMAL_POINT.format(hours / 24D) + " days";
        }

        final long minutes = TimeUnit.SECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toSeconds(minutes);
        if (hours != 0) {
            if (minutes == 0) {
                return hours + (hours > 1 ? " hours" : " hour");
            }

            return hours + FORMAT_DECIMAL_POINT.format(minutes / 60D) + " hours";
        }

        final long seconds = TimeUnit.SECONDS.toSeconds(time);
        if (minutes != 0) {
            if (seconds == 0) {
                return minutes + (minutes > 1 ? " minutes" : " minute");
            }

            return minutes + FORMAT_DECIMAL_POINT.format(seconds / 60D) + " minutes";
        }

        return seconds + (seconds > 1 ? " seconds" : " second");
    }

    public String getFormattedNumber(final long number) {
        return FORMAT_NUMBER.format(number);
    }

    protected String getFormattedUnixTime(final long unix) {
        return FORMAT_DATE.format(Date.from(Instant.ofEpochSecond(unix)));
    }

    public void checkApiResponse(final CommandParameters commandParameters, final ResponseModel response, final String errorTitle) {
        if (response instanceof ErrorModel) {
            throw new CommandReturnException(
                    UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                            .setTitle(errorTitle)
                            .setDescription(((ErrorModel) response).getErrorMessage())
                            .addField("Args", String.join(" ", commandParameters.getArgs()), false),
                    CommandResult.ERROR
            );
        }
    }

    protected void sendHelpMessage(final CommandParameters commandParameters, final String userArg, final int argPos, final String argName,
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

                final CommandParameters newCommandParameters = new CommandParameters(commandParameters);
                newCommandParameters.getArgs()[argPos] = similarNames[index];

                emotes.put(emote, new CommandEmoteReaction(this, newCommandParameters));
            }

            description.append("\n").append(DiscordEmotes.FOLDER.getEmote()).append(MarkdownUtil.bold("All " + argName + "s"));
        }

        final CommandParameters newCommandParameters = new CommandParameters(commandParameters);
        newCommandParameters.setArgs(newArgs);
        emotes.put(DiscordEmotes.FOLDER.getEmote(), new CommandEmoteReaction(command, newCommandParameters));

        this.sendEmoteMessage(commandParameters, "Invalid " + UtilitiesString.capitalize(argName), description.toString(), emotes);
    }

    protected int getStartPosition(final CommandParameters commandParameters, final int argPos, final int upperLimit) {
        final String name;
        if (argPos >= commandParameters.getArgs().length) {
            name = "1";
        } else {
            name = commandParameters.getArgs()[argPos];
        }

        if (!UtilitiesData.isInt(name)) {
            throw new CommandReturnException(
                    UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                            .setTitle("Invalid start position")
                            .setDescription(MarkdownUtil.monospace(name) + " is not a valid start position for the leaderboard.\n" +
                                    "Use a number between " + MarkdownUtil.bold("1") + " and " + MarkdownUtil.bold(String.valueOf(upperLimit)))
            );
        }

        return Math.min(Math.max(1, Integer.parseInt(name)), upperLimit);
    }

    protected int getEndPosition(final int startPos, final CommandParameters commandParameters, final int argPos, final int upperLimit) {
        final String name;
        if (argPos >= commandParameters.getArgs().length) {
            name = String.valueOf(upperLimit);
        } else {
            name = commandParameters.getArgs()[argPos];
        }

        if (!UtilitiesData.isInt(name)) {
            throw new CommandReturnException(
                    UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                            .setTitle("Invalid end position")
                            .setDescription(MarkdownUtil.monospace(name) + " is not a valid end position for the leaderboard.\n" +
                                    "Use a number between " + MarkdownUtil.bold("1") + " and " + MarkdownUtil.bold(String.valueOf(upperLimit)))
            );
        }

        int endPos = Integer.parseInt(name);
        if (startPos > endPos || endPos - startPos > MAX_LEADERBOARD_POSITION_DISTANCE) {
            endPos = startPos + MAX_LEADERBOARD_POSITION_DISTANCE;
        }

        return Math.min(Math.max(1, endPos), upperLimit);
    }
}
