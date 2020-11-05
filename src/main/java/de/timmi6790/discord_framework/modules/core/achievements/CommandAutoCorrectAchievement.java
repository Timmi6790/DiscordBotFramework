package de.timmi6790.discord_framework.modules.core.achievements;

import de.timmi6790.discord_framework.modules.achievement.achievements.StatValueAchievement;
import de.timmi6790.discord_framework.modules.core.stats.IncorrectArgCommandStat;
import de.timmi6790.discord_framework.modules.setting.settings.CommandAutoCorrectSetting;
import de.timmi6790.discord_framework.modules.user.UserDb;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.util.List;

public class CommandAutoCorrectAchievement extends StatValueAchievement {
    public CommandAutoCorrectAchievement() {
        super("Learn to use me", IncorrectArgCommandStat.class, 100);
    }

    @Override
    public void onUnlock(final UserDb userDb) {
        userDb.grantSetting(CommandAutoCorrectSetting.class);
    }

    @Override
    public List<String> getUnlockedPerks() {
        return Arrays.asList(new String[]{
                "A new setting. Check the " + MarkdownUtil.monospace("settings") + " command for more information."
        });
    }
}
