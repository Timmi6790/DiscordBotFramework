package de.timmi6790.discord_framework.modules.dsgvo;

import lombok.Data;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

@Data
public class UserData {
    private final long discordId;
    private final @NonNull Timestamp registerDate;
    private final long shopPoints;
    private final boolean isBanned;
    private final @NonNull String mainRank;
    private final @Nullable Map<String, Timestamp> achievements;
    private final @Nullable Map<String, Long> stats;
    private final @Nullable Set<String> secondaryRanks;
    private final @Nullable Map<String, String> settings;
    private final @Nullable Set<String> playerSpecificPermissions;
}
