package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.achievement.repository.AchievementRepository;
import de.timmi6790.discord_framework.modules.achievement.repository.AchievementRepositoryMysql;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public class AchievementModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractAchievement> achievements = new HashMap<>();
    private final Map<String, Integer> nameIdMatching = new CaseInsensitiveMap<>();

    private AchievementRepository achievementRepository;
    private EventModule eventModule;

    public AchievementModule() {
        super("Achievement");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                EventModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.achievementRepository = new AchievementRepositoryMysql(this);
        this.eventModule = this.getModuleOrThrow(EventModule.class);
    }

    public void registerAchievements(final AbstractAchievement... achievements) {
        for (final AbstractAchievement achievement : achievements) {
            if (!this.registerAchievement(achievement)) {
                DiscordBot.getLogger().warn("Achievement {} is already registered.", achievement.getName());
            }
        }
    }

    public boolean registerAchievement(final AbstractAchievement achievement) {
        if (this.achievements.containsKey(achievement.getDatabaseId())) {
            return false;
        }

        achievement.setDatabaseId(this.achievementRepository.retrieveOrCreateSettingId(achievement.getInternalName()));
        this.achievements.put(achievement.getDatabaseId(), achievement);
        this.nameIdMatching.put(achievement.getInternalName(), achievement.getDatabaseId());

        this.eventModule.addEventListener(achievement);
        return true;
    }

    public void grantAchievement(@NonNull final UserDb userDb, @NonNull final AbstractAchievement achievement) {
        if (!userDb.getAchievements().add(achievement.getDatabaseId())) {
            return;
        }

        this.achievementRepository.grantPlayerAchievement(userDb.getDatabaseId(), achievement.getDatabaseId());
        achievement.onUnlock(userDb);
    }
}
