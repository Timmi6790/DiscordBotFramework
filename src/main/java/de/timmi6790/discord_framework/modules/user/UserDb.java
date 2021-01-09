package de.timmi6790.discord_framework.modules.user;

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
import lombok.NonNull;
import lombok.ToString;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;


@Data
@EqualsAndHashCode(exclude = {"userDbModule", "achievementModule", "settingModule", "statModule", "eventModule", "rankModule"})
@ToString(exclude = {"userDbModule", "achievementModule", "settingModule", "statModule", "eventModule", "rankModule"})
public class UserDb {
    private final int databaseId;
    private final long discordId;
    private final Set<Integer> rankIds;
    private final Set<Integer> permissionIds;
    private final Map<Integer, String> settingsMap;
    private final Map<Integer, Integer> stats;
    private final Set<Integer> achievementIds;
    private int primaryRankId;
    private boolean banned;

    private final UserDbModule userDbModule;
    @Nullable
    private final AchievementModule achievementModule;
    @Nullable
    private final SettingModule settingModule;
    @Nullable
    private final StatModule statModule;
    private final EventModule eventModule;
    private final RankModule rankModule;

    public UserDb(final UserDbModule userDbModule,
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

        this.userDbModule = userDbModule;
        this.eventModule = eventModule;
        this.settingModule = settingModule;
        this.statModule = statModule;
        this.rankModule = rankModule;
    }

    protected UserDbRepository getUserDbRepository() {
        return this.userDbModule.getUserDbRepository();
    }

    public User getUser() {
        return this.userDbModule.getDiscordUserCache().get(this.getDiscordId());
    }

    public void ban(final CommandParameters commandParameters, final String reason) {
        this.setBanned(true);

        DiscordMessagesUtilities.sendPrivateMessage(
                commandParameters.getUser(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("You are banned")
                        .setDescription(
                                "Congratulations!!! You did it. You are now banned from using this bot for "
                                        + MarkdownUtil.monospace(reason) + "."
                        )
        );
    }

    public boolean setBanned(final boolean newBanStatus) {
        if (this.banned == newBanStatus) {
            return false;
        }

        this.getUserDbRepository().setBanStatus(this.getDatabaseId(), newBanStatus);
        this.banned = newBanStatus;
        return true;
    }

    // Permission
    public Set<Integer> getAllPermissionIds() {
        final Set<Integer> permissionSet = new HashSet<>(this.permissionIds);

        this.rankModule.getRank(this.primaryRankId)
                .ifPresent(rank -> permissionSet.addAll(rank.getPermissionIds(true)));
        for (final int rankId : this.getRankIds()) {
            this.rankModule.getRank(rankId).ifPresent(rank -> permissionSet.addAll(rank.getPermissionIds(true)));
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
        return this.hasPrimaryRank(rank.getRepositoryId());
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
        return this.setPrimaryRankId(rank.getRepositoryId());
    }

    public boolean hasRank(final int rankId) {
        return this.rankIds.contains(rankId);
    }

    public boolean hasRank(@NonNull final Rank rank) {
        return this.hasRank(rank.getRepositoryId());
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
        return this.addRank(rank.getRepositoryId());
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
        return this.removeRank(rank.getRepositoryId());
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
        return this.achievementIds.contains(achievement.getRepositoryId());
    }

    public boolean grantAchievement(@NonNull final AbstractAchievement achievement, final boolean sendUnlockMessage) {
        if (this.hasAchievement(achievement)) {
            return false;
        }

        this.getUserDbRepository().grantPlayerAchievement(this.getDatabaseId(), achievement.getRepositoryId());
        this.achievementIds.add(achievement.getRepositoryId());
        achievement.onUnlock(this);
        if (sendUnlockMessage) {
            // Show all the perks the players unlocked with this achievement
            final StringJoiner perks = new StringJoiner("\n");
            for (final String unlocked : achievement.getUnlockedPerks()) {
                perks.add("- " + unlocked);
            }

            final User user = this.getUser();
            // Always inform the user only inside his dms about the unlocked achievement to reduce spam
            DiscordMessagesUtilities.sendPrivateMessage(
                    user,
                    DiscordMessagesUtilities.getEmbedBuilder(user, null)
                            .setTitle("Achievement Unlocked")
                            .setDescription(
                                    "Unlocked: %s%n%nPerks:%n%s",
                                    MarkdownUtil.bold(achievement.getAchievementName()),
                                    perks.toString()
                            )
            );
        }
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
        this.setStatValue(
                stat,
                this.getStatValue(stat).orElse(0) + value
        );
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
                this.userDbModule.getDiscordBot().getBaseShard(),
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
                                .orElseGet(() -> String.valueOf(setting.getDefaultValue()))
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
        return this.getSettingOrDefault(
                CommandAutoCorrectSetting.class,
                Boolean.FALSE
        );
    }
}
