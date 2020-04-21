package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands;

import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.errors.ErrorModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class AbstractStatsCommand extends AbstractCommand {
    protected final static String UNKNOWN_POSITION = "Unknown";
    protected final static String UNKNOWN_SCORE = ">1000";

    private final static SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private final static DecimalFormat FORMAT_DECIMAL_POINT = new DecimalFormat(".##");

    static {
        final DecimalFormatSymbols symbols = FORMAT_DECIMAL_POINT.getDecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        FORMAT_DECIMAL_POINT.setDecimalFormatSymbols(symbols);
    }

    public AbstractStatsCommand(final String name, final String category, final String description, final String syntax, final String... aliasNames) {
        super(name, category, description, syntax, aliasNames);
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

    protected String getFormattedUnixTime(final long unix) {
        return FORMAT_DATE.format(Date.from(Instant.ofEpochSecond(unix))) + "UTC";
    }

    protected String getApiErrorMessage(final ResponseModel responseModel) {
        if (responseModel instanceof ErrorModel) {
            return ((ErrorModel) responseModel).getErrorMessage();
        }

        return "Unknown Error";
    }
}
