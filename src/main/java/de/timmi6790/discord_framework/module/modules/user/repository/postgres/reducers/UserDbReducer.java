package de.timmi6790.discord_framework.module.modules.user.repository.postgres.reducers;

import de.timmi6790.discord_framework.module.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.module.modules.stat.StatModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Log4j2
public class UserDbReducer implements LinkedHashMapRowReducer<Long, UserDb> {
    private final RankModule rankModule;
    private final @Nullable AchievementModule achievementModule;
    private final @Nullable SettingModule settingModule;
    private final @Nullable StatModule statModule;

    private Optional<AbstractSetting<?>> getSetting(final RowView rowView) {
        if (this.settingModule != null) {
            final Integer settingId = rowView.getColumn("setting_id", Integer.class);
            if (settingId != null) {
                return this.settingModule.getSetting(settingId);
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractStat> getStat(final RowView rowView) {
        if (this.statModule != null) {
            final Integer statId = rowView.getColumn("stat_id", Integer.class);
            if (statId != null) {
                return this.statModule.getStat(statId);
            }

        }
        return Optional.empty();
    }

    @Override
    public void accumulate(final Map<Long, UserDb> container, final RowView rowView) {
        final UserDb userDb = container.computeIfAbsent(
                rowView.getColumn("discord_id", Long.class),
                id -> rowView.getRow(UserDb.class)
        );

        // Ranks
        final Integer rankId = rowView.getColumn("rank_id", Integer.class);
        if (rankId != null) {
            final Optional<Rank> rankOpt = this.rankModule.getRank(rankId);
            if (rankOpt.isPresent()) {
                userDb.addRankRepositoryOnly(rankOpt.get());
            } else {
                log.warn(
                        "Can't find rank {} for user {}",
                        rankId,
                        userDb.getDiscordId()
                );
            }
        }

        // Permission ids
        final Integer permissionId = rowView.getColumn("permission_id", Integer.class);
        if (permissionId != null) {
            userDb.addPermissionRepositoryOnly(permissionId);
        }

        // Settings
        final Optional<AbstractSetting<?>> settingOpt = this.getSetting(rowView);
        if (settingOpt.isPresent()) {
            final String settingValue = rowView.getColumn("setting_value", String.class);
            userDb.addSettingRepositoryOnly(settingOpt.get(), settingValue);
        }

        // Stats
        final Optional<AbstractStat> statOpt = this.getStat(rowView);
        if (statOpt.isPresent()) {
            final Integer statValue = rowView.getColumn("stat_value", Integer.class);
            userDb.addStatRepositoryOnly(statOpt.get(), statValue);
        }

        // Achievements
        if (this.achievementModule != null) {
            final Integer achievementId = rowView.getColumn("achievement_id", Integer.class);
            if (achievementId != null) {
                final Optional<AbstractAchievement> achievementOpt = this.achievementModule.getAchievement(achievementId);
                if (achievementOpt.isPresent()) {
                    userDb.addAchievementRepositoryOnly(achievementOpt.get());
                } else {
                    log.warn(
                            "Can't find achievement {} for user {}",
                            achievementId,
                            userDb.getDiscordId()
                    );
                }
            }
        }
    }
}
