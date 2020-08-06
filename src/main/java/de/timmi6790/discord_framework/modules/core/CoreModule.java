package de.timmi6790.discord_framework.modules.core;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.core.commands.info.AboutCommand;
import de.timmi6790.discord_framework.modules.core.commands.info.AccountDeletionCommand;
import de.timmi6790.discord_framework.modules.core.commands.info.InviteCommand;
import de.timmi6790.discord_framework.modules.core.commands.management.BotInfoCommand;
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
                CommandModule.class,
                AchievementModule.class,
                StatModule.class,
                SettingModule.class
        );
    }

    @Override
    public void onEnable() {
        DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new AboutCommand(),
                new BotInfoCommand(),
                new AccountDeletionCommand()
        );

        final String inviteUrl = DiscordBot.getConfig().getString("discord.inviteUrl");
        if (inviteUrl != null && !inviteUrl.isEmpty()) {
            DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).registerCommands(
                    this,
                    new InviteCommand(inviteUrl)
            );
        }

        DiscordBot.getModuleManager().getModule(SettingModule.class)
                .ifPresent(settingModule -> {
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

