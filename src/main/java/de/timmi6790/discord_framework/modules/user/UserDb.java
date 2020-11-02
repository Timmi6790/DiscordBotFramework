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
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.modules.stat.StatModule;
import de.timmi6790.discord_framework.modules.stat.events.StatsChangeEvent;
import de.timmi6790.discord_framework.modules.user.repository.UserDbRepository;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Data
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
    private final Set<Integer> ranks;
    private final long points;
    private final Set<Integer> permissionIds;
    private final Map<Integer, String> settings;
    private final Map<Integer, Integer> stats;
    private final Set<Integer> achievements;
    private final UserDbRepository userDbRepository;
    private int primaryRank;
    private boolean banned;

    public UserDb(final UserDbRepository userDbRepository,
                  final int databaseId,
                  final long discordId,
                  final int primaryRank,
                  final Set<Integer> ranks,
                  final boolean banned,
                  final long points,
                  final Set<Integer> permissionIds,
                  final Map<Integer, String> settings,
                  final Map<Integer, Integer> stats,
                  final Set<Integer> achievements) {
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
        this.userDbRepository = userDbRepository;
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

        final RankModule rankModule = DiscordBot.getInstance().getModuleManager().getModuleOrThrow(RankModule.class);
        rankModule.getRank(this.primaryRank).ifPresent(rank -> permissionSet.addAll(rank.getAllPermissions()));
        this.ranks.stream()
                .map(rankModule::getRank)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(rank -> permissionSet.addAll(rank.getAllPermissions()));

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
        return this.primaryRank == rankId;
    }

    public boolean hasPrimaryRank(@NonNull final Rank rank) {
        return this.hasPrimaryRank(rank.getDatabaseId());
    }

    public boolean setPrimaryRank(final int rankId) {
        if (rankId == this.primaryRank) {
            return false;
        }

        this.getUserDbRepository().setPrimaryRank(this.getDatabaseId(), rankId);
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

        this.getUserDbRepository().addRank(this.getDatabaseId(), rankId);
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

        this.getUserDbRepository().removeRank(this.getDatabaseId(), rankId);
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

    public void grantAchievement(@NonNull final AbstractAchievement achievement) {
        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(AchievementModule.class).grantAchievement(this, achievement);
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

        final StatsChangeEvent statsChangeEvent = new StatsChangeEvent(this, stat, currentValueOpt.orElse(-1), value);
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
        this.getUserDbRepository().grantSetting(this.getDatabaseId(), setting.getDatabaseId(), defaultValue);
        this.settings.put(setting.getDatabaseId(), defaultValue);

        return true;
    }

    public boolean hasAutoCorrection() {
        // TODO: Reimplement me
        return false;
    }
}
