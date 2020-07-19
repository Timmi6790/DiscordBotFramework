package de.timmi6790.discord_framework.modules.core.achievements;

import de.timmi6790.discord_framework.modules.achievement.achievements.StatValueAchievement;
import de.timmi6790.discord_framework.modules.core.UserDb;
import de.timmi6790.discord_framework.modules.core.settings.CommandAutoCorrectSetting;
import de.timmi6790.discord_framework.modules.core.stats.IncorrectArgCommandStat;

public class CommandAutoCorrectAchievement extends StatValueAchievement {
    public CommandAutoCorrectAchievement() {
        super("Learn to use me", "core.achievement.auto_correction", IncorrectArgCommandStat.class, 100);
    }

    @Override
    public void onUnlock(final UserDb userDb) {
        userDb.grantSetting(CommandAutoCorrectSetting.class);
    }
}
