package de.timmi6790.discord_framework.utilities;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.temporal.TemporalAccessor;

@UtilityClass
public class DateUtilities {
    public ZoneOffset getZoneOffset(final TemporalAccessor temporal) {
        return getZoneOffset(temporal, ZoneOffset.UTC);
    }

    public ZoneOffset getZoneOffset(@NonNull final TemporalAccessor temporal, @NonNull final ZoneOffset defaultZone) {
        try {
            return ZoneOffset.from(temporal);
        } catch (final DateTimeException ignore) {
            return defaultZone;
        }
    }

    public OffsetDateTime convertToOffsetDate(@NonNull final TemporalAccessor temporal) {
        final ZoneOffset offset = DateUtilities.getZoneOffset(temporal);

        try {
            return OffsetDateTime.of(LocalDateTime.from(temporal), offset);
        } catch (final DateTimeException ignore) {
        }

        try {
            return OffsetDateTime.ofInstant(Instant.from(temporal), offset);
        } catch (final DateTimeException exception) {
            throw new DateTimeException(
                    String.format(
                            "Unable to obtain OffsetDateTime from TemporalAccessor: %s of type %s",
                            temporal,
                            temporal.getClass().getName()
                    ),
                    exception
            );
        }
    }
}
