package de.timmi6790.discord_framework.module.modules.core;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.core.achievements.CommandAutoCorrectAchievement;
import de.timmi6790.discord_framework.module.modules.core.stats.FailedCommandResultStat;
import de.timmi6790.discord_framework.module.modules.core.stats.IncorrectArgCommandResultStat;
import de.timmi6790.discord_framework.module.modules.core.stats.MissingArgCommandResultStat;
import de.timmi6790.discord_framework.module.modules.core.stats.SuccessfulCommandResultStat;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.stat.StatModule;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

/**
 * Contains command, achievements, stats and settings that would no make sense everywhere else This is supposed to be
 * removed in the future
 */
@EqualsAndHashCode(callSuper = true)
@Log4j2
public class CoreModule extends AbstractModule {
    /**
     * Instantiates a new Core module.
     */
    public CoreModule() {
        super("Core");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class,
                SlashCommandModule.class,
                AchievementModule.class,
                StatModule.class,
                SettingModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.getModule(AchievementModule.class)
                .ifPresent(achievementModule -> achievementModule.registerAchievements(
                                this,
                                new CommandAutoCorrectAchievement()
                        )
                );

        this.getModule(StatModule.class)
                .ifPresent(statModule -> {
                            log.info("Registering stats");
                            statModule.registerStats(
                                    this,
                                    new FailedCommandResultStat(),
                                    new MissingArgCommandResultStat(),
                                    new SuccessfulCommandResultStat(),
                                    new IncorrectArgCommandResultStat()
                            );
                        }
                );
        return true;
    }
}

