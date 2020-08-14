package de.timmi6790.discord_framework.modules.user;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.event.events.StatsChangeEvent;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.modules.stat.StatModule;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Data
public class UserDb {
    private static final String UPDATE_PLAYER_BAN_STATUS = "UPDATE player SET banned = :banned WHERE id = :databaseId LIMIT 1;";

    private static final String UPDATE_STAT_VALUE = "UPDATE player_stat SET `value` = :value WHERE player_id = :playerId AND stat_id = :statId LIMIT 1;";
    private static final String INSERT_STAT_VALUE = "INSERT player_stat(player_id, stat_id, value) VALUES(:playerId, :statId, :value)";

    private static final String INSERT_PLAYER_ACHIEVEMENT = "INSERT player_achievement(player_id, achievement_id) VALUES(:playerId, :achievementId)";

    private static final String INSERT_PLAYER_SETTING = "INSERT player_setting(player_id, setting_id, setting) VALUES(:playerId, :settingId, :setting);";

    private static final String INSERT_PLAYER_PERMISSION = "INSERT INTO player_permission(player_id, permission_id) VALUES(:playerId, :permissionId);";
    private static final String DELETE_PLAYER_PERMISSION = "DELETE FROM player_permission WHERE player_id = :playerId AND permission_id = :permissionId LIMIT 1";

    private static final String SET_PRIMARY_RANK = "UPDATE `player` SET player.primary_rank = :primaryRank WHERE player.id = :databaseId LIMIT 1;";
    private static final String ADD_RANK = "INSERT INTO player_rank(player_id, rank_id) VALUES(:databaseId, :rankId);";
    private static final String DELETE_RANK = "DELETE FROM player_rank WHERE player_rank.player_id = :databaseId AND player_rank.rank_id = :rankId LIMIT 1;";

    @Getter
    private static final LoadingCache<Long, User> userCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build(key -> DiscordBot.getInstance().getDiscord().retrieveUserById(key, false).complete());

    private final int databaseId;
    private final long discordId;

    private int primaryRank;
    private final Set<Integer> ranks;

    private boolean banned;

    private final long points;

    private final Set<Integer> permissionIds;
    private final Map<Integer, String> settings;
    private final Map<Integer, Integer> stats;
    private final Set<Integer> achievements;

    public UserDb(final int databaseId, final long discordId, final int primaryRank, final Set<Integer> ranks, final boolean banned, final long points, final Set<Integer> permissionIds,
                  final Map<Integer, String> settings, final Map<Integer, Integer> stats, final Set<Integer> achievements) {
        this.databaseId = databaseId;
        this.discordId = discordId;
        this.primaryRank = primaryRank;
        this.ranks = ranks;
        this.banned = banned;
        this.points = points;
        this.permissionIds = permissionIds;
        this.settings = settings;
        this.stats = stats;
        this.achievements = achievements;
    }

    public User getUser() {
        return userCache.get(this.getDiscordId());
    }

    public void ban(final CommandParameters commandParameters, final String reason) {
        this.setBanned(true);

        DiscordMessagesUtilities.sendPrivateMessage(
                commandParameters.getUser(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("You are banned")
                        .setDescription("Congratulations!!! You did it. You are now banned from using this bot for " + MarkdownUtil.monospace(reason) + ".")
        );
    }

    public boolean setBanned(final boolean banned) {
        if (this.banned == banned) {
            return false;
        }

        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(UPDATE_PLAYER_BAN_STATUS)
                        .bind("banned", banned ? 1 : 0)
                        .bind("databaseId", this.databaseId)
                        .execute()
        );
        this.banned = banned;
        return true;
    }

    // Permission
    public Set<Integer> getAllPermissionIds() {
        final Set<Integer> permissionSet = new HashSet<>(this.permissionIds);

        final RankModule rankModule = DiscordBot.getInstance().getModuleManager().getModuleOrThrow(RankModule.class);
        rankModule.getRank(this.primaryRank).ifPresent(rank -> permissionSet.addAll(rank.getAllPermissions()));
        this.ranks.stream()
                .map(rankModule::getRank)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(rank -> permissionSet.addAll(rank.getAllPermissions()));

        return permissionSet;
    }

    public boolean hasPermission(final int id) {
        return this.permissionIds.contains(id);
    }

    public boolean addPermission(final int id) {
        if (this.hasPermission(id)) {
            return false;
        }

        this.permissionIds.add(id);
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_PERMISSION)
                        .bind("playerId", this.databaseId)
                        .bind("permissionId", id)
                        .execute()
        );
        return true;
    }

    public boolean removePermission(final int id) {
        if (!this.hasPermission(id)) {
            return false;
        }

        this.permissionIds.remove(id);
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(DELETE_PLAYER_PERMISSION)
                        .bind("playerId", this.databaseId)
                        .bind("permissionId", id)
                        .execute()
        );
        return true;
    }

    // Ranks
    public boolean hasPrimaryRank(final int rankId) {
        return this.primaryRank == rankId;
    }

    public boolean hasPrimaryRank(@NonNull final Rank rank) {
        return this.hasPrimaryRank(rank.getDatabaseId());
    }

    public boolean setPrimaryRank(final int rankId) {
        if (rankId == this.primaryRank) {
            return false;
        }

        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(SET_PRIMARY_RANK)
                        .bind("primaryRank", rankId)
                        .bind("databaseId", this.databaseId)
                        .execute()
        );
        this.primaryRank = rankId;

        return true;
    }

    public boolean setPrimaryRank(@NonNull final Rank rank) {
        return this.setPrimaryRank(rank.getDatabaseId());
    }

    public boolean hasRank(final int rankId) {
        return this.ranks.contains(rankId);
    }

    public boolean hasRank(@NonNull final Rank rank) {
        return this.hasRank(rank.getDatabaseId());
    }

    public boolean addRank(final int rankId) {
        if (this.hasRank(rankId)) {
            return false;
        }

        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(ADD_RANK)
                        .bind("rankId", rankId)
                        .bind("databaseId", this.databaseId)
                        .execute()
        );
        this.ranks.add(rankId);

        return true;
    }

    public boolean addRank(@NonNull final Rank rank) {
        return this.addRank(rank.getDatabaseId());
    }

    public boolean removeRank(final int rankId) {
        if (!this.hasRank(rankId)) {
            return false;
        }

        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(DELETE_RANK)
                        .bind("rankId", rankId)
                        .bind("databaseId", this.databaseId)
                        .execute()
        );
        this.ranks.remove(rankId);

        return true;
    }

    public boolean removeRank(@NonNull final Rank rank) {
        return this.removeRank(rank.getDatabaseId());
    }

    // Achievements
    public boolean hasAchievement(final AbstractAchievement achievement) {
        return this.achievements.contains(achievement.getDatabaseId());
    }

    public void grantAchievement(final AbstractAchievement achievement) {
        if (!this.achievements.add(achievement.getDatabaseId())) {
            return;
        }

        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
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
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(sqlQuery)
                        .bind("playerId", this.getDatabaseId())
                        .bind("statId", stat.getDatabaseId())
                        .bind("value", value)
                        .execute()
        );

        this.stats.put(stat.getDatabaseId(), value);

        final StatsChangeEvent statsChangeEvent = new StatsChangeEvent(this, stat, currentValue, value);
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(EventModule.class).executeEvent(statsChangeEvent);
    }

    public Map<AbstractStat, Integer> getStatsMap() {
        final Optional<StatModule> statModuleOpt = DiscordBot.getInstance().getModuleManager().getModule(StatModule.class);
        return statModuleOpt.map(statModule -> this.stats.entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(statModule.getStats().get(entry.getKey()), entry.getValue()))
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)))
                .orElseGet(HashMap::new);
    }

    // Settings
    public boolean grantSetting(final Class<? extends AbstractSetting<?>> settingClass) {
        final Optional<SettingModule> settingModule = DiscordBot.getInstance().getModuleManager().getModule(SettingModule.class);
        if (!settingModule.isPresent()) {
            return false;
        }

        return settingModule.get()
                .getSettings()
                .values()
                .stream()
                .filter(setting -> setting.getClass().isAssignableFrom(settingClass))
                .findAny()
                .filter(this::grantSetting)
                .isPresent();
    }

    public boolean grantSetting(final AbstractSetting<?> setting) {
        if (this.settings.containsKey(setting.getDatabaseId())) {
            return false;
        }

        final String defaultValue = setting.getDefaultValue();
        this.settings.put(setting.getDatabaseId(), defaultValue);
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(INSERT_PLAYER_SETTING)
                        .bind("playerId", this.getDatabaseId())
                        .bind("settingId", setting.getDatabaseId())
                        .bind("setting", defaultValue)
                        .execute()
        );

        return true;
    }

    public boolean hasAutoCorrection() {
        // TODO: Reimplement me
        return false;
    }
}
