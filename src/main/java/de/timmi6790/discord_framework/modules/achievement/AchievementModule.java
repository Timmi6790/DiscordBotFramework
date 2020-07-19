package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.core.UserDb;
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
        super("AchievementModule");
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

                    DiscordBot.getEventManager().addEventListener(achievement);
                });
    }

    public void grantAchievement(final UserDb userDb, final AbstractAchievement achievement) {
        userDb.grantAchievement(achievement);
    }
}
