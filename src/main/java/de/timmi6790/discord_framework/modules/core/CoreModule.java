package de.timmi6790.discord_framework.modules.core;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.core.commands.info.*;
import de.timmi6790.discord_framework.modules.core.commands.management.BotInfoCommand;
import de.timmi6790.discord_framework.modules.core.commands.management.RankCommand;
import de.timmi6790.discord_framework.modules.core.commands.management.UserCommand;
import de.timmi6790.discord_framework.modules.core.database.ChannelDbMapper;
import de.timmi6790.discord_framework.modules.core.database.GuildDbMapper;
import de.timmi6790.discord_framework.modules.core.database.UserDbMapper;
import de.timmi6790.discord_framework.modules.core.settings.CommandAutoCorrectSetting;
import de.timmi6790.discord_framework.modules.core.stats.FailedCommandStat;
import de.timmi6790.discord_framework.modules.core.stats.IncorrectArgCommandStat;
import de.timmi6790.discord_framework.modules.core.stats.MissingArgCommandStat;
import de.timmi6790.discord_framework.modules.core.stats.SuccessfulCommandStat;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.stat.StatModule;
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
        DiscordBot.getDatabase()
                .registerRowMapper(ChannelDb.class, new ChannelDbMapper())
                .registerRowMapper(GuildDb.class, new GuildDbMapper())
                .registerRowMapper(UserDb.class, new UserDbMapper());

        DiscordBot.getCommandManager().registerCommands(
                new HelpCommand(),
                new AboutCommand(),
                new BotInfoCommand(),
                new AccountDeletionCommand(),
                new UserCommand(),
                new RankCommand()
        );

        final String inviteUrl = DiscordBot.getConfig().getString("discord.inviteUrl");
        if (inviteUrl != null && !inviteUrl.isEmpty()) {
            DiscordBot.getCommandManager().registerCommands(
                    new InviteCommand(inviteUrl)
            );
        }

        DiscordBot.getModuleManager().getModule(SettingModule.class)
                .ifPresent(settingModule -> {
                            DiscordBot.getCommandManager().registerCommands(
                                    new SettingsCommand()
                            );

                            this.logger.info("Registering settings");
                            settingModule.registerSettings(
                                    new CommandAutoCorrectSetting()

                            );
                        }
                );

        DiscordBot.getModuleManager().getModule(StatModule.class)
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

        DiscordBot.getModuleManager().getModule(AchievementModule.class)
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

