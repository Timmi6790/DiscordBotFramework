package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public class AchievementModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractAchievement> achievements = new HashMap<>();
    private final Map<String, Integer> nameIdMatching = new CaseInsensitiveMap<>();

    public AchievementModule() {
        super("Achievement");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                EventModule.class
        );
    }

    public void registerAchievements(final AbstractAchievement... achievements) {
        for (final AbstractAchievement achievement : achievements) {
            if (this.achievements.containsKey(achievement.getDatabaseId())) {
                DiscordBot.getLogger().warn("Achievement {} is already registered.", achievement.getName());
                continue;
            }

            this.achievements.put(achievement.getDatabaseId(), achievement);
            this.nameIdMatching.put(achievement.getInternalName(), achievement.getDatabaseId());

            this.getModuleOrThrow(EventModule.class).addEventListener(achievement);
        }
    }

    public void grantAchievement(final UserDb userDb, final AbstractAchievement achievement) {
        userDb.grantAchievement(achievement);
    }
}
