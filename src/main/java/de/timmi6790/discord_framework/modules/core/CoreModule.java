package de.timmi6790.discord_framework.modules.core;


import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.core.achievements.CommandAutoCorrectAchievement;
import de.timmi6790.discord_framework.modules.core.commands.info.InviteCommand;
import de.timmi6790.discord_framework.modules.core.stats.FailedCommandResultStat;
import de.timmi6790.discord_framework.modules.core.stats.IncorrectArgCommandResultStat;
import de.timmi6790.discord_framework.modules.core.stats.MissingArgCommandResultStat;
import de.timmi6790.discord_framework.modules.core.stats.SuccessfulCommandResultStat;
import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.OptionalDependency;
import de.timmi6790.discord_framework.modules.stat.StatModule;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

/**
 * Contains command, achievements, stats and settings that would no make sense everywhere else This is supposed to be
 * removed in the future
 */
@EqualsAndHashCode
@Log4j2
public class CoreModule implements Module {
    /**
     * Instantiates a new Core module.
     */
    public CoreModule(final CommandModule commandModule,
                      final ConfigModule configModule,
                      @OptionalDependency final AchievementModule achievementModule,
                      @OptionalDependency final StatModule statModule) {
        // TODO: FIX ME
        /*
        commandModule.registerCommands(
                this,
                new BotInfoCommand()
        );

         */

        final String inviteUrl = configModule
                .registerAndGetConfig(this, new Config())
                .getInviteUrl();

        if (inviteUrl != null && !inviteUrl.isEmpty()) {
            commandModule.registerCommands(
                    this,
                    new InviteCommand(inviteUrl)
            );
        }

        if (achievementModule != null) {
            achievementModule.registerAchievements(
                    this,
                    new CommandAutoCorrectAchievement()
            );
        }

        if (statModule != null) {
            log.info("Registering stats");
            statModule.registerStats(
                    this,
                    new FailedCommandResultStat(),
                    new MissingArgCommandResultStat(),
                    new SuccessfulCommandResultStat(),
                    new IncorrectArgCommandResultStat()
            );
        }
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getAuthors() {
        return new String[]{"Timmi6790"};
    }
}

