package de.timmi6790.discord_framework.modules.user;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.setting.settings.CommandAutoCorrectSetting;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.modules.stat.StatModule;
import de.timmi6790.discord_framework.modules.stat.events.StatsChangeEvent;
import de.timmi6790.discord_framework.modules.user.repository.UserDbRepository;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Data
@EqualsAndHashCode(exclude = {"userDbRepository", "achievementModule", "settingModule", "statModule", "eventModule", "rankModule"})
public class UserDb {
    @Getter
    private static final LoadingCache<Long, User> USER_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build(key -> {
                final CompletableFuture<User> futureValue = new CompletableFuture<>();
                DiscordBot.getInstance().getDiscord().retrieveUserById(key, false).queue(futureValue::complete);
                return futureValue.get(1, TimeUnit.MINUTES);
            });

    private final int databaseId;
    private final long discordId;
    private final Set<Integer> rankIds;
    private final Set<Integer> permissionIds;
    private final Map<Integer, String> settingsMap;
    private final Map<Integer, Integer> stats;
    private final Set<Integer> achievementIds;
    private final UserDbRepository userDbRepository;
    @Nullable
    private final AchievementModule achievementModule;
    @Nullable
    private final SettingModule settingModule;
    @Nullable
    private final StatModule statModule;
    private final EventModule eventModule;
    private final RankModule rankModule;
    private int primaryRankId;
    private boolean banned;

    public UserDb(final UserDbRepository userDbRepository,
                  final EventModule eventModule,
                  final RankModule rankModule,
                  @Nullable final AchievementModule achievementModule,
                  @Nullable final SettingModule settingModule,
                  @Nullable final StatModule statModule,
                  final int databaseId,
                  final long discordId,
                  final int primaryRankId,
                  final Set<Integer> rankIds,
                  final boolean banned,
                  final Set<Integer> permissionIds,
                  final Map<Integer, String> settingsMap,
                  final Map<Integer, Integer> stats,
                  final Set<Integer> achievementIds) {
        this.databaseId = databaseId;
        this.discordId = discordId;
        this.primaryRankId = primaryRankId;
        this.rankIds = rankIds;
        this.banned = banned;
        this.permissionIds = permissionIds;
        this.settingsMap = settingsMap;
        this.stats = stats;
        this.achievementIds = achievementIds;
        this.achievementModule = achievementModule;
        this.userDbRepository = userDbRepository;
        this.eventModule = eventModule;
        this.settingModule = settingModule;
        this.statModule = statModule;
        this.rankModule = rankModule;
    }

    public User getUser() {
        return USER_CACHE.get(this.getDiscordId());
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

        this.userDbRepository.setBanStatus(this.getDatabaseId(), banned);
        this.banned = banned;
        return true;
    }

    // Permission
    public Set<Integer> getAllPermissionIds() {
        final Set<Integer> permissionSet = new HashSet<>(this.permissionIds);

        this.rankModule.getRank(this.primaryRankId)
                .ifPresent(rank -> permissionSet.addAll(rank.getAllPermissionIds()));
        for (final int rankId : this.getRankIds()) {
            this.rankModule.getRank(rankId).ifPresent(rank -> permissionSet.addAll(rank.getAllPermissionIds()));
        }

        return permissionSet;
    }

    public boolean hasPermission(final int permissionId) {
        return this.permissionIds.contains(permissionId);
    }

    public boolean addPermission(final int permissionId) {
        if (this.hasPermission(permissionId)) {
            return false;
        }

        this.getUserDbRepository().addPermission(this.getDatabaseId(), permissionId);
        this.permissionIds.add(permissionId);

        return true;
    }

    public boolean removePermission(final int permissionId) {
        if (!this.hasPermission(permissionId)) {
            return false;
        }

        this.permissionIds.remove(permissionId);
        this.getUserDbRepository().removePermission(this.getDatabaseId(), permissionId);

        return true;
    }

    // Ranks
    public boolean hasPrimaryRank(final int rankId) {
        return this.primaryRankId == rankId;
    }

    public boolean hasPrimaryRank(@NonNull final Rank rank) {
        return this.hasPrimaryRank(rank.getDatabaseId());
    }

    public boolean setPrimaryRankId(final int rankId) {
        if (rankId == this.primaryRankId) {
            return false;
        }

        this.getUserDbRepository().setPrimaryRank(this.getDatabaseId(), rankId);
        this.primaryRankId = rankId;

        return true;
    }

    public boolean setPrimaryRank(@NonNull final Rank rank) {
        return this.setPrimaryRankId(rank.getDatabaseId());
    }

    public boolean hasRank(final int rankId) {
        return this.rankIds.contains(rankId);
    }

    public boolean hasRank(@NonNull final Rank rank) {
        return this.hasRank(rank.getDatabaseId());
    }

    public boolean addRank(final int rankId) {
        if (this.hasRank(rankId)) {
            return false;
        }

        this.getUserDbRepository().addRank(this.getDatabaseId(), rankId);
        this.rankIds.add(rankId);

        return true;
    }

    public boolean addRank(@NonNull final Rank rank) {
        return this.addRank(rank.getDatabaseId());
    }

    public boolean removeRank(final int rankId) {
        if (!this.hasRank(rankId)) {
            return false;
        }

        this.getUserDbRepository().removeRank(this.getDatabaseId(), rankId);
        this.rankIds.remove(rankId);

        return true;
    }

    public boolean removeRank(@NonNull final Rank rank) {
        return this.removeRank(rank.getDatabaseId());
    }

    // Achievements
    public List<AbstractAchievement> getAchievements() {
        if (this.achievementModule == null) {
            return new ArrayList<>();
        }

        final List<AbstractAchievement> achievements = new ArrayList<>();
        for (final int achievementId : this.getAchievementIds()) {
            this.achievementModule.getAchievement(achievementId).ifPresent(achievements::add);
        }
        return achievements;
    }

    public boolean hasAchievement(@NonNull final AbstractAchievement achievement) {
        return this.achievementIds.contains(achievement.getDatabaseId());
    }

    public boolean grantAchievement(@NonNull final AbstractAchievement achievement) {
        if (this.hasAchievement(achievement)) {
            return false;
        }

        this.userDbRepository.grantPlayerAchievement(this.getDatabaseId(), achievement.getDatabaseId());
        this.achievementIds.add(achievement.getDatabaseId());
        achievement.onUnlock(this);
        return true;
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
        final Optional<Integer> currentValueOpt = this.getStatValue(stat);

        if (currentValueOpt.isPresent()) {
            this.getUserDbRepository().updateStat(this.getDatabaseId(), stat.getDatabaseId(), value);
        } else {
            this.getUserDbRepository().insertStat(this.getDatabaseId(), stat.getDatabaseId(), value);
        }

        this.stats.put(stat.getDatabaseId(), value);

        final StatsChangeEvent statsChangeEvent = new StatsChangeEvent(
                DiscordBot.getInstance().getDiscord(),
                this,
                stat,
                currentValueOpt.orElse(-1),
                value
        );

        this.eventModule.executeEvent(statsChangeEvent);
    }

    public Map<AbstractStat, Integer> getStatsMap() {
        if (this.statModule == null) {
            return new HashMap<>();
        }

        final Map<AbstractStat, Integer> statMap = new HashMap<>();
        for (final Map.Entry<Integer, Integer> entry : this.getStats().entrySet()) {
            this.statModule.getStat(entry.getKey()).ifPresent(stat -> statMap.put(stat, entry.getValue()));
        }
        return statMap;
    }

    // Settings
    private <T> Optional<? extends AbstractSetting<T>> getSettingFromClass(final Class<? extends AbstractSetting<T>> settingClazz) {
        if (this.settingModule == null) {
            return Optional.empty();
        }

        return this.settingModule.getSetting(settingClazz);
    }

    public Map<AbstractSetting<?>, String> getSettings() {
        if (this.settingModule == null) {
            return new HashMap<>();
        }

        final Map<AbstractSetting<?>, String> playerSettings = new HashMap<>();
        for (final AbstractSetting<?> setting : this.settingModule.getSettings().values()) {
            if (this.hasSetting(setting)) {
                playerSettings.put(
                        setting,
                        this.getSetting(setting)
                                .map(String::valueOf)
                                .orElse(String.valueOf(setting.getDefaultValue()))
                );
            }
        }

        return playerSettings;
    }

    public <T> boolean hasSetting(final Class<? extends AbstractSetting<T>> settingClazz) {
        final Optional<? extends AbstractSetting<T>> settingOpt = this.getSettingFromClass(settingClazz);
        return settingOpt.filter(this::hasSetting).isPresent();
    }

    public boolean hasSetting(final AbstractSetting<?> setting) {
        return this.getAllPermissionIds().contains(setting.getPermissionId());
    }

    public <T> boolean grantSetting(final Class<? extends AbstractSetting<T>> settingClazz) {
        final Optional<? extends AbstractSetting<T>> settingOpt = this.getSettingFromClass(settingClazz);
        return settingOpt.filter(this::grantSetting).isPresent();
    }

    public <T> boolean grantSetting(final AbstractSetting<T> setting) {
        if (this.hasSetting(setting)) {
            return false;
        }

        this.addPermission(setting.getPermissionId());
        return true;
    }

    public <T> void setSetting(final Class<? extends AbstractSetting<T>> settingClazz, final T value) {
        this.getSettingFromClass(settingClazz).ifPresent(setting -> this.setSetting(setting, value));
    }

    public <T> void setSetting(final AbstractSetting<T> setting, final T value) {
        final String newValue = setting.toDatabaseValue(value);
        if (this.settingsMap.containsKey(setting.getDatabaseId())) {
            this.getUserDbRepository().updateSetting(this.getDatabaseId(), setting.getDatabaseId(), newValue);
        } else {
            this.getUserDbRepository().grantSetting(this.getDatabaseId(), setting.getDatabaseId(), newValue);
        }

        this.settingsMap.put(setting.getDatabaseId(), newValue);
    }

    public <T> Optional<T> getSetting(final Class<? extends AbstractSetting<T>> settingClazz) {
        final Optional<? extends AbstractSetting<T>> settingOpt = this.getSettingFromClass(settingClazz);
        if (settingOpt.isPresent()) {
            return this.getSetting(settingOpt.get());
        }
        return Optional.empty();
    }

    public <T> Optional<T> getSetting(final AbstractSetting<T> setting) {
        // Check if the player has the permissions for the setting.
        if (!this.hasSetting(setting)) {
            return Optional.empty();
        }

        final String value = this.settingsMap.get(setting.getDatabaseId());
        if (value != null) {
            return Optional.ofNullable(setting.fromDatabaseValue(value));
        }

        return Optional.of(setting.getDefaultValue());
    }

    public <T> T getSettingOrDefault(final Class<? extends AbstractSetting<T>> settingClazz, final T defaultValue) {
        final Optional<? extends AbstractSetting<T>> settingOpt = this.getSettingFromClass(settingClazz);
        if (settingOpt.isPresent()) {
            return this.getSettingOrDefault(settingOpt.get(), defaultValue);
        }
        return defaultValue;
    }

    public <T> T getSettingOrDefault(final AbstractSetting<T> setting, final T defaultValue) {
        return this.getSetting(setting).orElse(defaultValue);
    }

    public boolean hasAutoCorrection() {
        return this.getSettingOrDefault(CommandAutoCorrectSetting.class, false);
    }
}
