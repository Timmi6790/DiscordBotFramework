package de.timmi6790.discord_framework.module.modules.user;

import de.timmi6790.discord_framework.module.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.module.modules.command_old.CommandParameters;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.setting.settings.CommandAutoCorrectSetting;
import de.timmi6790.discord_framework.module.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.module.modules.stat.events.StatsChangeEvent;
import de.timmi6790.discord_framework.module.modules.user.repository.UserDbRepository;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;


@Data
@AllArgsConstructor
public class UserDb {
    private final long discordId;
    private final Set<Rank> ranks = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<Integer> permissionIds = new HashSet<>();
    // TODO: Maybe already convert it and instead save it as object
    private final Map<AbstractSetting<?>, String> settings = new WeakHashMap<>();
    private final Map<AbstractStat, Integer> stats = new WeakHashMap<>();
    private final Set<AbstractAchievement> achievements = Collections.newSetFromMap(new WeakHashMap<>());
    private Rank primaryRank;
    private boolean banned;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final UserDbModule userDbModule;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final EventModule eventModule;
    @Nullable
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final SettingModule settingModule;

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

        this.getUserDbRepository().setBanStatus(this.discordId, newBanStatus);
        this.banned = newBanStatus;
        return true;
    }

    // Permission
    public Set<Integer> getAllPermissionIds() {
        final Set<Integer> permissionSet = new HashSet<>(this.permissionIds);

        permissionSet.addAll(this.primaryRank.getPermissionIds(true));
        for (final Rank rank : this.getRanks()) {
            permissionSet.addAll(rank.getPermissionIds(true));
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

        this.getUserDbRepository().addPermission(this.discordId, permissionId);
        this.permissionIds.add(permissionId);

        return true;
    }

    public boolean removePermission(final int permissionId) {
        if (!this.hasPermission(permissionId)) {
            return false;
        }

        this.permissionIds.remove(permissionId);
        this.getUserDbRepository().removePermission(this.discordId, permissionId);

        return true;
    }

    // Ranks
    public boolean hasPrimaryRank(@NonNull final Rank rank) {
        return this.primaryRank == rank;
    }

    public boolean setPrimaryRank(@NonNull final Rank rank) {
        if (this.primaryRank == rank) {
            return false;
        }

        this.getUserDbRepository().setPrimaryRank(this.discordId, rank.getRepositoryId());
        this.primaryRank = rank;

        return true;
    }

    public boolean hasRank(@NonNull final Rank rank) {
        return this.ranks.contains(rank);
    }

    public boolean addRank(@NonNull final Rank rank) {
        if (this.hasRank(rank)) {
            return false;
        }

        this.getUserDbRepository().addRank(this.discordId, rank.getRepositoryId());
        this.ranks.add(rank);

        return true;
    }

    public boolean removeRank(@NonNull final Rank rank) {
        if (!this.hasRank(rank)) {
            return false;
        }

        this.getUserDbRepository().removeRank(this.discordId, rank.getRepositoryId());
        this.ranks.remove(rank);

        return true;
    }

    // Achievements
    public boolean hasAchievement(@NonNull final AbstractAchievement achievement) {
        return this.achievements.contains(achievement);
    }

    public boolean grantAchievement(@NonNull final AbstractAchievement achievement, final boolean sendUnlockMessage) {
        if (this.hasAchievement(achievement)) {
            return false;
        }

        this.getUserDbRepository().grantAchievement(this.discordId, achievement.getRepositoryId());
        this.achievements.add(achievement);
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
        return Optional.ofNullable(this.stats.get(stat));
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
            this.getUserDbRepository().updateStat(this.discordId, stat.getDatabaseId(), value);
        } else {
            this.getUserDbRepository().insertStat(this.discordId, stat.getDatabaseId(), value);
        }

        this.stats.put(stat, value);

        final StatsChangeEvent statsChangeEvent = new StatsChangeEvent(
                this.userDbModule.getDiscordBot().getBaseShard(),
                this,
                stat,
                currentValueOpt.orElse(-1),
                value
        );

        this.eventModule.executeEvent(statsChangeEvent);
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
        return this.getSettingFromClass(settingClazz)
                .filter(this::hasSetting)
                .isPresent();
    }

    public boolean hasSetting(final AbstractSetting<?> setting) {
        return this.getAllPermissionIds()
                .contains(setting.getPermissionId());
    }

    public <T> boolean grantSetting(final Class<? extends AbstractSetting<T>> settingClazz) {
        return this.getSettingFromClass(settingClazz)
                .filter(this::grantSetting)
                .isPresent();
    }

    public <T> boolean grantSetting(final AbstractSetting<T> setting) {
        if (this.hasSetting(setting)) {
            return false;
        }

        this.addPermission(setting.getPermissionId());
        return true;
    }

    public <T> void setSetting(final Class<? extends AbstractSetting<T>> settingClazz, final T value) {
        this.getSettingFromClass(settingClazz)
                .ifPresent(setting -> this.setSetting(setting, value));
    }

    public <T> void setSetting(final AbstractSetting<T> setting, final T value) {
        final String newValue = setting.toDatabaseValue(value);
        if (this.settings.containsKey(setting)) {
            this.getUserDbRepository().updateSetting(
                    this.discordId,
                    setting.getDatabaseId(),
                    newValue
            );
        } else {
            this.getUserDbRepository().grantSetting(
                    this.discordId,
                    setting.getDatabaseId(),
                    newValue
            );
        }

        this.settings.put(setting, newValue);
    }

    public <T> Optional<T> getSetting(final Class<? extends AbstractSetting<T>> settingClazz) {
        return this.getSettingFromClass(settingClazz)
                .flatMap(this::getSetting);
    }

    public <T> Optional<T> getSetting(final AbstractSetting<T> setting) {
        // Check if the player has the permissions for the setting.
        if (!this.hasSetting(setting)) {
            return Optional.empty();
        }

        final String value = this.settings.get(setting);
        if (value != null) {
            return Optional.ofNullable(setting.fromDatabaseValue(value));
        }

        return Optional.of(setting.getDefaultValue());
    }

    public <T> T getSettingOrDefault(final Class<? extends AbstractSetting<T>> settingClazz, final T defaultValue) {
        return this.getSettingFromClass(settingClazz)
                .map(setting -> this.getSettingOrDefault(setting, defaultValue))
                .orElse(defaultValue);
    }

    public <T> T getSettingOrDefault(final AbstractSetting<T> setting, final T defaultValue) {
        return this.getSetting(setting)
                .orElse(defaultValue);
    }

    public boolean hasAutoCorrection() {
        return this.getSettingOrDefault(
                CommandAutoCorrectSetting.class,
                Boolean.FALSE
        );
    }

    // Repository help methods
    public void addRankRepositoryOnly(@Nonnull final Rank rank) {
        this.ranks.add(rank);
    }

    public void addPermissionRepositoryOnly(final int permissionId) {
        this.permissionIds.add(permissionId);
    }

    public void addSettingRepositoryOnly(@Nonnull final AbstractSetting<?> setting, @Nonnull final String value) {
        this.settings.put(setting, value);
    }

    public void addStatRepositoryOnly(@Nonnull final AbstractStat stat, final int value) {
        this.stats.put(stat, value);
    }

    public void addAchievementRepositoryOnly(@Nonnull final AbstractAchievement achievement) {
        this.achievements.add(achievement);
    }
}
