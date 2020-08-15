package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
public class AchievementModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractAchievement> achievements = new ConcurrentHashMap<>();
    private final Map<String, Integer> nameIdMatching = new ConcurrentHashMap<>();

    public AchievementModule() {
        super("Achievement");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class
        );
    }

    @Override
    public void onInitialize() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public void registerAchievements(final AbstractAchievement... achievements) {
        Arrays.stream(achievements)
                .filter(abstractAchievement -> !this.achievements.containsKey(abstractAchievement.getDatabaseId()))
                .forEach(achievement -> {
                    this.achievements.put(achievement.getDatabaseId(), achievement);
                    this.nameIdMatching.put(achievement.getInternalName(), achievement.getDatabaseId());

                    this.getModuleOrThrow(EventModule.class).addEventListener(achievement);
                });
    }

    public void grantAchievement(final UserDb userDb, final AbstractAchievement achievement) {
        userDb.grantAchievement(achievement);
    }
}
