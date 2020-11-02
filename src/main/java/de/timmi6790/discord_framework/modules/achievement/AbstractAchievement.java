package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.Data;

@Data
public abstract class AbstractAchievement {
    private final String name;
    private final String internalName;
    private int databaseId;

    protected AbstractAchievement(final String name, final String internalName) {

        this.name = name;
        this.internalName = internalName;
    }

    public void unlockAchievement(final UserDb userDb) {
        userDb.grantAchievement(this);
    }

    public abstract void onUnlock(UserDb userDb);
}
