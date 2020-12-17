package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.achievement.repository.AchievementRepository;
import de.timmi6790.discord_framework.modules.achievement.repository.AchievementRepositoryMysql;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public void registerAchievements(@NonNull final AbstractModule module,
                                     final AbstractAchievement... achievements) {
        for (final AbstractAchievement achievement : achievements) {
            this.registerAchievement(module, achievement);
        }
    }

    public boolean registerAchievement(@NonNull final AbstractModule module,
                                       @NonNull final AbstractAchievement achievement) {
        if (this.achievements.containsKey(achievement.getDatabaseId())) {
            return false;
        }

        achievement.setInternalName(this.generateInternalName(module, "achievement", achievement.getName()));
        achievement.setDatabaseId(this.achievementRepository.retrieveOrCreateSettingId(achievement.getInternalName()));

        this.achievements.put(achievement.getDatabaseId(), achievement);
        this.nameIdMatching.put(achievement.getName(), achievement.getDatabaseId());

        this.eventModule.addEventListener(achievement);
        return true;
    }

    public Optional<AbstractAchievement> getAchievement(@NonNull final String achievementName) {
        final Integer achievementId = this.nameIdMatching.get(achievementName);
        if (achievementId != null) {
            return this.getAchievement(achievementId);
        }

        return Optional.empty();
    }

    public Optional<AbstractAchievement> getAchievement(final int achievementId) {
        return Optional.ofNullable(this.achievements.get(achievementId));
    }
}
