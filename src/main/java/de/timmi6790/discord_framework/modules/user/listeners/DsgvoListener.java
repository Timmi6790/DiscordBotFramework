package de.timmi6790.discord_framework.modules.user.listeners;

import de.timmi6790.discord_framework.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.dsgvo.events.UserDataDeleteEvent;
import de.timmi6790.discord_framework.modules.dsgvo.events.UserDataRequestEvent;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class DsgvoListener {
    private static final String FALLBACK_NAME = "Unknown";

    private final UserDbModule userDbModule;

    @SubscribeEvent
    public void onUserDataDelete(final UserDataDeleteEvent event) {
        this.userDbModule.delete(event.getUserDb());
    }

    @SubscribeEvent
    public void onUserDataRequest(final UserDataRequestEvent event) {
        final UserDb userDb = event.getUserDb();

        event.addData("discordId", userDb.getDiscordId());
        event.addData("isBanned", userDb.isBanned());

        // Achievements
        this.userDbModule.getModule(AchievementModule.class).ifPresent(achievementModule -> {
            final Set<String> achievementNames = new HashSet<>();
            for (final int achievementId : userDb.getAchievementIds()) {
                achievementNames.add(
                        achievementModule.getAchievement(achievementId)
                                .map(AbstractAchievement::getAchievementName)
                                .orElse(FALLBACK_NAME)
                );
            }
            event.addData("achievements", achievementNames);
        });

        // Ranks
        this.userDbModule.getModule(RankModule.class).ifPresent(rankModule -> {
            final Set<String> subRankNames = new HashSet<>();
            for (final int subRankId : userDb.getRankIds()) {
                subRankNames.add(
                        rankModule.getRank(subRankId)
                                .map(Rank::getRankName)
                                .orElse(FALLBACK_NAME)
                );
            }

            event.addData(
                    "mainRank",
                    rankModule.getRank(userDb.getPrimaryRankId())
                            .map(Rank::getRankName)
                            .orElse(FALLBACK_NAME)
            );
            event.addData("subRanks", subRankNames);
        });

        // Stats
        final Map<String, Integer> parsedStats = new HashMap<>();
        for (final Map.Entry<AbstractStat, Integer> entry : userDb.getStatsMap().entrySet()) {
            parsedStats.put(
                    entry.getKey().getName(),
                    entry.getValue()
            );
        }
        event.addData("stats", parsedStats);

        // Settings
        this.userDbModule.getModule(SettingModule.class).ifPresent(settingModule -> {
            final Map<String, Object> parsedSettings = new HashMap<>();
            for (final Map.Entry<AbstractSetting<?>, String> entry : userDb.getSettings().entrySet()) {
                parsedSettings.put(
                        entry.getKey().getStatName(),
                        entry.getKey().fromDatabaseValue(entry.getValue())
                );
            }
            event.addData("settings", parsedSettings);
        });

        // Perms
        this.userDbModule.getModule(PermissionsModule.class).ifPresent(permissionsModule -> {
            final Set<String> parsedPermissions = new HashSet<>();
            for (final Integer permissionId : userDb.getPermissionIds()) {
                parsedPermissions.add(
                        permissionsModule.getPermissionFromId(permissionId)
                                .orElse(FALLBACK_NAME)
                );
            }
            event.addData("playerSpecificPermissions", parsedPermissions);
        });
    }
}
