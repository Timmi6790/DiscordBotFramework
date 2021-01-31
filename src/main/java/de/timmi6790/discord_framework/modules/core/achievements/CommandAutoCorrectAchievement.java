package de.timmi6790.discord_framework.modules.core.achievements;

import de.timmi6790.discord_framework.modules.achievement.achievements.StatValueAchievement;
import de.timmi6790.discord_framework.modules.core.stats.IncorrectArgCommandResultStat;
import de.timmi6790.discord_framework.modules.setting.settings.CommandAutoCorrectSetting;
import de.timmi6790.discord_framework.modules.user.UserDb;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Collections;
import java.util.List;

/**
 * Award the {@link CommandAutoCorrectSetting} after reaching a value of 100 in {@link IncorrectArgCommandResultStat}
 */
public class CommandAutoCorrectAchievement extends StatValueAchievement {
    /**
     * Instantiates a new Command auto correct achievement.
     */
    public CommandAutoCorrectAchievement() {
        super(
                "Learn to use me",
                IncorrectArgCommandResultStat.class,
                100
        );
    }

    @Override
    public void onUnlock(final UserDb userDb) {
        userDb.grantSetting(CommandAutoCorrectSetting.class);
    }

    @Override
    public List<String> getUnlockedPerks() {
        return Collections.singletonList(
                "A new setting. Check the " + MarkdownUtil.monospace("settings") + " command for more information."
        );
    }
}
