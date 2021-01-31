package de.timmi6790.discord_framework.modules.core;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.core.achievements.CommandAutoCorrectAchievement;
import de.timmi6790.discord_framework.modules.core.commands.info.BotInfoCommand;
import de.timmi6790.discord_framework.modules.core.commands.info.InviteCommand;
import de.timmi6790.discord_framework.modules.core.stats.FailedCommandResultStat;
import de.timmi6790.discord_framework.modules.core.stats.IncorrectArgCommandResultStat;
import de.timmi6790.discord_framework.modules.core.stats.MissingArgCommandResultStat;
import de.timmi6790.discord_framework.modules.core.stats.SuccessfulCommandResultStat;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.stat.StatModule;
import lombok.EqualsAndHashCode;

/**
 * Contains command, achievements, stats and settings that would no make sense everywhere else This is supposed to be
 * removed in the future
 */
@EqualsAndHashCode(callSuper = true)
public class CoreModule extends AbstractModule {
    /**
     * Instantiates a new Core module.
     */
    public CoreModule() {
        super("Core");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class,
                CommandModule.class,
                AchievementModule.class,
                StatModule.class,
                SettingModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new BotInfoCommand()
        );

        final String inviteUrl = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config())
                .getInviteUrl();
        if (inviteUrl != null && !inviteUrl.isEmpty()) {
            this.getModuleOrThrow(CommandModule.class).registerCommands(
                    this,
                    new InviteCommand(inviteUrl)
            );
        }

        this.getModule(AchievementModule.class)
                .ifPresent(achievementModule -> achievementModule.registerAchievements(
                        this,
                        new CommandAutoCorrectAchievement()
                        )
                );

        this.getModule(StatModule.class)
                .ifPresent(statModule -> {
                            DiscordBot.getLogger().info("Registering stats");
                            statModule.registerStats(
                                    this,
                                    new FailedCommandResultStat(),
                                    new MissingArgCommandResultStat(),
                                    new SuccessfulCommandResultStat(),
                                    new IncorrectArgCommandResultStat()
                            );
                        }
                );
    }
}

