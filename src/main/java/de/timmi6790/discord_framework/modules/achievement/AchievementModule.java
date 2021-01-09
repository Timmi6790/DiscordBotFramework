package de.timmi6790.discord_framework.modules.achievement;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.achievement.repository.AchievementRepository;
import de.timmi6790.discord_framework.modules.achievement.repository.mysql.AchievementRepositoryMysql;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.event.EventModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles all achievements inside the bot
 */
@EqualsAndHashCode(callSuper = true)
public class AchievementModule extends AbstractModule {
    @Getter
    private final Map<Integer, AbstractAchievement> achievements = new HashMap<>();
    private final Map<String, Integer> nameIdMatching = new CaseInsensitiveMap<>();

    private AchievementRepository achievementRepository;
    private EventModule eventModule;

    /**
     * Instantiates a new Achievement module.
     */
    public AchievementModule() {
        super("Achievement");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                EventModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.achievementRepository = new AchievementRepositoryMysql(this.getModuleOrThrow(DatabaseModule.class));
        this.eventModule = this.getModuleOrThrow(EventModule.class);
    }

    /**
     * Tries to register the achievement with the given module. The module is required because the internal achievement
     * name is based on the module it is used by
     *
     * @param module       the module
     * @param achievements the achievements
     */
    public void registerAchievements(@NonNull final AbstractModule module,
                                     final AbstractAchievement... achievements) {
        for (final AbstractAchievement achievement : achievements) {
            this.registerAchievement(module, achievement);
        }
    }

    /**
     * Tries to register the achievement with the given module. The module is required because the internal achievement
     * name is based on the module it is used by
     *
     * @param module      the module
     * @param achievement the achievement
     * @return did register correctly
     */
    public boolean registerAchievement(@NonNull final AbstractModule module,
                                       @NonNull final AbstractAchievement achievement) {
        if (this.achievements.containsKey(achievement.getRepositoryId())) {
            return false;
        }

        // Create the internal achievement name for the repository
        final String internalAchievementName = this.generateInternalName(
                module,
                "achievement",
                achievement.getAchievementName()
        );
        achievement.setInternalAchievementName(internalAchievementName);

        // Get or create the repository id
        final int achievementRepositoryId = this.achievementRepository.retrieveOrCreateAchievementId(achievement.getInternalAchievementName());
        achievement.setRepositoryId(achievementRepositoryId);

        this.achievements.put(achievement.getRepositoryId(), achievement);
        this.nameIdMatching.put(achievement.getAchievementName(), achievement.getRepositoryId());

        this.eventModule.addEventListener(achievement);
        return true;
    }

    /**
     * Gets the achievement with the given achievement name
     *
     * @param achievementName the achievement name
     * @return the achievement
     */
    public Optional<AbstractAchievement> getAchievement(@NonNull final String achievementName) {
        final Integer achievementId = this.nameIdMatching.get(achievementName);
        if (achievementId != null) {
            return this.getAchievement(achievementId);
        }

        return Optional.empty();
    }

    /**
     * Gets the achievement with the given internal id
     *
     * @param internalAchievementId the internal achievement id
     * @return the achievement
     */
    public Optional<AbstractAchievement> getAchievement(final int internalAchievementId) {
        return Optional.ofNullable(this.achievements.get(internalAchievementId));
    }
}
