package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.errors.ErrorModel;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesData;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
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

    public void checkApiResponse(final CommandParameters commandParameters, final ResponseModel response, final String arguments) {
        if (response instanceof ErrorModel) {
            final ErrorModel errorModel = (ErrorModel) response;
            final EmbedBuilder embedBuilder = UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters);
            // No stats found
            if (errorModel.getErrorCode() == 1) {
                embedBuilder.setTitle("No stats found")
                        .setDescription("There are no collected stats.\n WIP")
                        .addField("Arguments", arguments, false);
            } else {
                embedBuilder.setTitle("Error")
                        .setDescription("Something went wrong while requesting your data.")
                        .addField("Api Response", errorModel.getErrorMessage(), false)
                        .setImage("https://media1.tenor.com/images/981ee5030a18a779e899b2c307e65f7a/tenor.gif?itemid=13159552");
            }

            throw new CommandReturnException(embedBuilder, CommandResult.ERROR);
        }
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

    protected long getUnixTime(final CommandParameters commandParameters, final int startArgPos) {
        if (startArgPos >= commandParameters.getArgs().length) {
            return Instant.now().getEpochSecond();
        }

        final String[] dateArgs = new String[commandParameters.getArgs().length - startArgPos];
        System.arraycopy(commandParameters.getArgs(), startArgPos, dateArgs, 0, dateArgs.length);
        final String name = String.join(" ", dateArgs);

        // TODO: Better date parsing :/
        final List<Date> dates = new PrettyTimeParser().parse(name);
        if (!dates.isEmpty()) {
            return dates.get(0).getTime() / 1_000;
        }

        throw new CommandReturnException(UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                .setTitle("Invalid Date")
                .setDescription(MarkdownUtil.monospace(name) + " is not a valid date.")
        );
    }
}
