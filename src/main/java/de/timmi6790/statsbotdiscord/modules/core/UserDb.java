package de.timmi6790.statsbotdiscord.modules.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.events.StatsChangeEvent;
import de.timmi6790.statsbotdiscord.modules.achievement.AbstractAchievement;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.core.settings.CommandAutoCorrectSetting;
import de.timmi6790.statsbotdiscord.modules.setting.AbstractSetting;
import de.timmi6790.statsbotdiscord.modules.setting.settings.BooleanSetting;
import de.timmi6790.statsbotdiscord.modules.stat.AbstractStat;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class UserDb {
    private static final String GET_PLAYER = "SELECT player.id, player.discordId, player.shop_points shopPoints, player.banned, player.primary_rank primaryRank, " +
            "GROUP_CONCAT(DISTINCT p_rank.rank_id) ranks, GROUP_CONCAT(DISTINCT permission.permission_node) AS perms, " +
            "GROUP_CONCAT(DISTINCT CONCAT_WS(',', p_setting.setting_id, p_setting.setting) SEPARATOR ';') settings, " +
            "GROUP_CONCAT(DISTINCT CONCAT_WS(',', p_stat.stat_id, p_stat.value) SEPARATOR ';') stats, " +
            "GROUP_CONCAT(DISTINCT p_ach.achievement_id) achievements " +
            "FROM player  " +
            "LEFT JOIN player_rank p_rank ON p_rank.player_id = player.id  " +
            "LEFT JOIN player_permission p_perm ON p_perm.player_id = player.id  " +
            "LEFT JOIN permission ON permission.default_permission = 1 OR permission.id = p_perm.permission_id  " +
            "LEFT JOIN player_setting p_setting ON p_setting.player_id = player.id  " +
            "LEFT JOIN player_stat p_stat ON p_stat.player_id = player.id " +
            "LEFT JOIN player_achievement p_ach ON p_ach.player_id = player.id " +
            "WHERE player.discordId = :discordId LIMIT 1;";
    private static final String INSERT_PLAYER = "INSERT INTO player(discordId) VALUES (:discordId);";

    private static final String BAN_PLAYER = "UPDATE player SET banned = :banned WHERE id = :databaseId LIMIT 1;";

    private static final String UPDATE_STAT_VALUE = "UPDATE player_stat SET `value` = :value WHERE player_id = :playerId AND stat_id = :statId LIMIT 1;";
    private static final String INSERT_STAT_VALUE = "INSERT player_stat(player_id, stat_id, value) VALUES(:playerId, :statId, :value)";

    private static final String INSERT_PLAYER_ACHIEVEMENT = "INSERT player_achievement(player_id, achievement_id) VALUES(:playerId, :achievementId)";

    private static final String INSERT_PLAYER_SETTING = "INSERT player_setting(player_id, setting_id, setting) VALUES(:playerId, :settingId, :setting);";

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

    private final Set<String> permissionNodes;
    private final Map<Integer, String> settings;
    private final Map<Integer, Integer> stats;
    private final Set<Integer> achievements;

    public static Optional<UserDb> get(final long discordId) {
        final UserDb userDb = USER_CACHE.getIfPresent(discordId);
        if (userDb != null) {
            return Optional.of(userDb);
        }

        final Optional<UserDb> userOpt = StatsBot.getDatabase().withHandle(handle ->
                handle.createQuery(GET_PLAYER)
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
                    handle.createUpdate(INSERT_PLAYER)
                            .bind("discordId", discordId)
                            .execute()
            );

            return UserDb.get(discordId).orElseThrow(RuntimeException::new);
        });
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(StatsBot.getDiscord().getUserById(this.discordId));
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
                handle.createUpdate(BAN_PLAYER)
                        .bind("banned", banned ? 1 : 0)
                        .bind("databaseId", this.databaseId)
                        .execute()
        );

        return true;
    }

    // Shop
    public boolean addPoints(final long points) {

        return true;
    }

    // Achievements
    public boolean hasAchievement(final AbstractAchievement achievement) {
        return this.achievements.contains(achievement.getDatabaseId());
    }

    public void grantAchievement(final AbstractAchievement achievement) {
        if (!this.achievements.add(achievement.getDatabaseId())) {
            return;
        }

        StatsBot.getDatabase().useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_ACHIEVEMENT)
                        .bind("playerId", this.getDatabaseId())
                        .bind("achievementId", achievement.getDatabaseId())
                        .execute()
        );
        achievement.onUnlock(this);
    }

    // Stats
    public Optional<Integer> getStatValue(final AbstractStat stat) {
        return Optional.ofNullable(this.stats.get(stat.getDatabaseId()));
    }

    public void increaseStat(final AbstractStat stat) {
        this.increaseStat(stat, 1);
    }

    public void increaseStat(final AbstractStat stat, final int value) {
        this.setStatValue(stat, this.getStatValue(stat).orElse(0) + value);
    }

    public void setStatValue(final AbstractStat stat, final int value) {
        final int currentValue = this.getStatValue(stat).orElse(-1);
        final String sqlQuery = currentValue == -1 ? INSERT_STAT_VALUE : UPDATE_STAT_VALUE;
        StatsBot.getDatabase().useHandle(handle ->
                handle.createUpdate(sqlQuery)
                        .bind("playerId", this.getDatabaseId())
                        .bind("statId", stat.getDatabaseId())
                        .bind("value", value)
                        .execute()
        );

        this.stats.put(stat.getDatabaseId(), value);

        final StatsChangeEvent statsChangeEvent = new StatsChangeEvent(this, stat, currentValue, value);
        StatsBot.getEventManager().callEvent(statsChangeEvent);
    }

    public Map<AbstractStat, Integer> getStatsMap() {
        return this.stats.entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(StatsBot.getStatManager().getStats().get(entry.getKey()), entry.getValue()))
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    // Settings
    public void grantSetting(final Class<? extends AbstractSetting> settingClass) {
        StatsBot.getSettingManager()
                .getSettings()
                .values()
                .stream()
                .filter(setting -> setting.getClass().isAssignableFrom(settingClass))
                .findAny()
                .ifPresent(this::grantSetting);
    }

    public void grantSetting(final AbstractSetting setting) {
        if (this.settings.containsKey(setting.getDatabaseId())) {
            return;
        }

        final String defaultValue = setting.getDefaultValue();
        this.settings.put(setting.getDatabaseId(), defaultValue);
        StatsBot.getDatabase().useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_SETTING)
                        .bind("playerId", this.getDatabaseId())
                        .bind("settingId", setting.getDatabaseId())
                        .bind("setting", defaultValue)
                        .execute()
        );
    }

    public Object getSettingValue(final String internalName) {
        return StatsBot.getSettingManager().getSetting(internalName).map(abstractSetting -> this.settings.get(abstractSetting.getDatabaseId()));
    }

    public List<AbstractSetting> getSettings() {
        return this.settings.keySet()
                .stream()
                .map(settingDbId -> StatsBot.getSettingManager().getSettings().get(settingDbId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Map<AbstractSetting, String> getSettingsMap() {
        return this.settings.entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(StatsBot.getSettingManager().getSettings().get(entry.getKey()), entry.getValue()))
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public boolean hasSettingAndEqualsTrue(final String internalName) {
        final Optional<AbstractSetting> setting = StatsBot.getSettingManager().getSetting(internalName);
        if (!setting.isPresent() || !this.settings.containsKey(setting.get().getDatabaseId())) {
            return false;
        }

        if (!(setting.get() instanceof BooleanSetting)) {
            return false;
        }

        return ((BooleanSetting) setting.get()).parseSetting(this.settings.get(setting.get().getDatabaseId()));
    }

    public boolean hasAutoCorrection() {
        return this.hasSettingAndEqualsTrue(CommandAutoCorrectSetting.INTERNAL_NAME);
    }
}
