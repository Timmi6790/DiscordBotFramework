package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.Data;

import java.util.List;

@Data
public abstract class AbstractAchievement {
    private final String name;
    private String internalName;
    private int databaseId;

    protected AbstractAchievement(final String name) {
        this.name = name;
    }

    public void unlockAchievement(final UserDb userDb) {
        userDb.grantAchievement(this);
    }

    public abstract void onUnlock(UserDb userDb);

    public abstract List<String> getUnlockedPerks();
}
