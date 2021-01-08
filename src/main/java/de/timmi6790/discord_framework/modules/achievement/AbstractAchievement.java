package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.Data;

import java.util.List;

/**
 * Abstract achievement.
 */
@Data
public abstract class AbstractAchievement {
    private final String achievementName;
    private String internalAchievementName;
    private int repositoryId;

    /**
     * Instantiates a new Abstract achievement.
     *
     * @param achievementName the achievement name
     */
    protected AbstractAchievement(final String achievementName) {
        this.achievementName = achievementName;
    }

    /**
     * Unlock player achievement.
     *
     * @param userDb            the user db
     * @param sendUnlockMessage the send unlock message
     */
    public void unlockPlayerAchievement(final UserDb userDb, final boolean sendUnlockMessage) {
        userDb.grantAchievement(this, sendUnlockMessage);
    }

    /**
     * Triggered after a user has been granted the achievement to allow further logic from the achievement. For example
     * granting specific perms on unlock
     *
     * @param userDb user who unlocked the achievement
     */
    public abstract void onUnlock(UserDb userDb);

    /**
     * Returns a list of unlocked perks for the unlock message
     *
     * @return the unlocked perks
     */
    public abstract List<String> getUnlockedPerks();
}
