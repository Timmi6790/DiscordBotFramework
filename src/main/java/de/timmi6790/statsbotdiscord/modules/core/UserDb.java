package de.timmi6790.statsbotdiscord.modules.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.core.settings.CommandAutoCorrectSetting;
import de.timmi6790.statsbotdiscord.modules.setting.AbstractSetting;
import de.timmi6790.statsbotdiscord.modules.setting.settings.BooleanSetting;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final Map<Integer, String> settings;

    public static Optional<UserDb> get(final long discordId) {
        final UserDb userDb = USER_CACHE.getIfPresent(discordId);
        if (userDb != null) {
            return Optional.of(userDb);
        }

        final Optional<UserDb> userOpt = StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT player.id, player.discordId, player.shop_points shopPoints, player.banned, player.primary_rank primaryRank, GROUP_CONCAT(DISTINCT p_rank.rank_id) ranks, GROUP_CONCAT(DISTINCT permission.permission_node) AS perms, GROUP_CONCAT(DISTINCT CONCAT_WS(',', p_setting.setting_id, p_setting.setting) SEPARATOR ';') settings " +
                        "FROM player " +
                        "LEFT JOIN player_rank p_rank ON p_rank.player_id = player.id " +
                        "LEFT JOIN player_permission p_perm ON p_perm.player_id = player.id " +
                        "LEFT JOIN permission ON permission.default_permission = 1 OR permission.id = p_perm.permission_id " +
                        "LEFT JOIN player_setting p_setting ON p_setting.player_id = player.id " +
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

    public void ban(final CommandParameters commandParameters, final String reason) {
        this.setBanned(true);

        UtilitiesDiscord.sendPrivateMessage(
                commandParameters.getEvent().getAuthor(),
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("You are banned")
                        .setDescription("Congratulations!!! You did it. You are now banned from using this bot for " + MarkdownUtil.monospace(reason) + ".")
        );
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

    public Object getStatValue(final String internalName) {
        return StatsBot.getSettingManager().getSetting(internalName).map(abstractSetting -> this.settings.get(abstractSetting.getDbId()));
    }

    public List<AbstractSetting> getStats() {
        return this.settings.keySet()
                .stream()
                .map(settingDbId -> StatsBot.getSettingManager().getSettings().get(settingDbId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Map<AbstractSetting, String> getStatsMap() {
        return this.settings.entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(StatsBot.getSettingManager().getSettings().get(entry.getKey()), entry.getValue()))
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public boolean hasSettingAndEqualsTrue(final String internalName) {
        final Optional<AbstractSetting> setting = StatsBot.getSettingManager().getSetting(internalName);
        if (!setting.isPresent() || !this.settings.containsKey(setting.get().getDbId())) {
            return false;
        }

        if (!(setting.get() instanceof BooleanSetting)) {
            return false;
        }

        return ((BooleanSetting) setting.get()).parseSetting(this.settings.get(setting.get().getDbId()));
    }

    public boolean hasAutoCorrection() {
        return this.hasSettingAndEqualsTrue(CommandAutoCorrectSetting.INTERNAL_NAME);
    }
}
