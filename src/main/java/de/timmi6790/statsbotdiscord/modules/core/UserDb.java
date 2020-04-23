package de.timmi6790.statsbotdiscord.modules.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.setting.Setting;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ToString
@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class UserDb {
    @Getter
    private final static Cache<Long, UserDb> USER_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final int databaseId;
    private final long discordId;

    private final Rank primaryRank;
    private final List<Rank> ranks;

    private final boolean banned;

    private final long points;

    private final List<String> permissionNodes;
    private final List<Setting> settings;

    public static Optional<UserDb> get(final long discordId) {
        final UserDb userDb = USER_CACHE.getIfPresent(discordId);
        if (userDb != null) {
            return Optional.of(userDb);
        }

        final Optional<UserDb> userOpt = StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT player.id, player.discordId, player.shop_points shopPoints, player.banned, player.primary_rank primaryRank, GROUP_CONCAT(DISTINCT p_rank.rank_id) ranks, GROUP_CONCAT(DISTINCT permission.permission_node) AS perms " +
                        "FROM player " +
                        "LEFT JOIN player_rank p_rank ON p_rank.player_id = player.id " +
                        "LEFT JOIN player_permission p_perm ON p_perm.player_id = player.id " +
                        "LEFT JOIN permission ON permission.default_permission = 1 OR permission.id = p_perm.permission_id " +
                        "WHERE player.discordId = :discordId LIMIT 1;")
                        .bind("discordId", discordId)
                        .mapTo(UserDb.class)
                        .findOne()
        );

        userOpt.ifPresent(u -> USER_CACHE.put(discordId, u));
        return userOpt;
    }

    public static UserDb getOrCreate(final long discordId) {
        return UserDb.get(discordId).orElseGet(() -> {
            StatsBot.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO player(discordId) VALUES (:discordId);")
                            .bind("discordId", discordId)
                            .execute()
            );

            return UserDb.get(discordId).orElseThrow(RuntimeException::new);
        });
    }

    public boolean setBanned(final boolean banned) {
        if (this.banned == banned) {
            return false;
        }

        StatsBot.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE player SET banned = :banned WHERE id = :databaseId LIMIT 1;")
                        .bind("banned", banned ? 1 : 0)
                        .bind("databaseId", this.databaseId)
                        .execute()
        );

        return true;
    }

    public boolean addPoints(final long points) {

        return true;
    }
}
