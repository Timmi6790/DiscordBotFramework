package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.commons.builders.MapBuilder;
import de.timmi6790.discord_framework.modules.database.DatabaseGetId;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.*;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
public abstract class AbstractAchievement extends DatabaseGetId {
    private static final String ACHIEVEMENT_NAME = "achievementName";

    private static final String GET_ACHIEVEMENT_ID = "SELECT id FROM `achievement` WHERE achievement_name = :achievementName LIMIT 1;";
    private static final String INSERT_NEW_ACHIEVEMENT = "INSERT INTO achievement(achievement_name) VALUES(:achievementName);";

    private final int databaseId;
    private final String name;
    private final String internalName;

    protected AbstractAchievement(final String name, final String internalName) {
        super(GET_ACHIEVEMENT_ID, INSERT_NEW_ACHIEVEMENT);

        this.name = name;
        this.internalName = internalName;
        this.databaseId = this.retrieveDatabaseId();
    }

    @Override
    protected @NonNull Map<String, Object> getGetIdParameters() {
        return MapBuilder.<String, Object>ofHashMap()
                .put(ACHIEVEMENT_NAME, this.getInternalName())
                .build();
    }

    @Override
    protected @NonNull Map<String, Object> getInsertIdParameters() {
        return MapBuilder.<String, Object>ofHashMap()
                .put(ACHIEVEMENT_NAME, this.getInternalName())
                .build();
    }

    public void unlockAchievement(final UserDb userDb) {
        userDb.grantAchievement(this);
    }

    public abstract void onUnlock(UserDb userDb);
}
