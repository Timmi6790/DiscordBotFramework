package de.timmi6790.statsbotdiscord.modules.core;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.AbstractModule;
import de.timmi6790.statsbotdiscord.modules.achievement.AchievementModule;
import de.timmi6790.statsbotdiscord.modules.core.commands.info.*;
import de.timmi6790.statsbotdiscord.modules.core.commands.management.BotInfoCommand;
import de.timmi6790.statsbotdiscord.modules.core.commands.management.RankCommand;
import de.timmi6790.statsbotdiscord.modules.core.commands.management.UserCommand;
import de.timmi6790.statsbotdiscord.modules.core.database.ChannelDbMapper;
import de.timmi6790.statsbotdiscord.modules.core.database.GuildDbMapper;
import de.timmi6790.statsbotdiscord.modules.core.database.UserDbMapper;
import de.timmi6790.statsbotdiscord.modules.core.settings.CommandAutoCorrectSetting;
import de.timmi6790.statsbotdiscord.modules.core.stats.FailedCommandStat;
import de.timmi6790.statsbotdiscord.modules.core.stats.IncorrectArgCommandStat;
import de.timmi6790.statsbotdiscord.modules.core.stats.MissingArgCommandStat;
import de.timmi6790.statsbotdiscord.modules.core.stats.SuccessfulCommandStat;
import de.timmi6790.statsbotdiscord.modules.setting.SettingModule;
import de.timmi6790.statsbotdiscord.modules.stat.StatModule;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EqualsAndHashCode(callSuper = true)
public class CoreModule extends AbstractModule {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CoreModule() {
        super("Core");

        this.addLoadAfter(
                AchievementModule.class,
                StatModule.class,
                SettingModule.class
        );
    }

    @Override
    public void onEnable() {
        StatsBot.getDatabase()
                .registerRowMapper(ChannelDb.class, new ChannelDbMapper())
                .registerRowMapper(GuildDb.class, new GuildDbMapper())
                .registerRowMapper(UserDb.class, new UserDbMapper());

        StatsBot.getCommandManager().registerCommands(
                new HelpCommand(),
                new AboutCommand(),
                new BotInfoCommand(),
                new InviteCommand(),
                new AccountDeletionCommand(),
                new SettingsCommand(),
                new UserCommand(),
                new RankCommand()
        );

        StatsBot.getModuleManager().getModule(SettingModule.class)
                .ifPresent(settingModule -> {
                            this.logger.info("Registering settings");
                            settingModule.registerSettings(
                                    new CommandAutoCorrectSetting()

                            );
                        }
                );

        StatsBot.getModuleManager().getModule(StatModule.class)
                .ifPresent(statModule -> {
                            this.logger.info("Registering stats");
                            statModule.registerStats(
                                    new FailedCommandStat(),
                                    new MissingArgCommandStat(),
                                    new SuccessfulCommandStat(),
                                    new IncorrectArgCommandStat()
                            );
                        }
                );

        StatsBot.getModuleManager().getModule(AchievementModule.class)
                .ifPresent(achievementModule ->
                        achievementModule.registerAchievements(
                                // new CommandAutoCorrectAchievement()
                        )
                );
    }

    @Override
    public void onDisable() {

    }
}

