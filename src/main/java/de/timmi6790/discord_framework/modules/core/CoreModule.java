package de.timmi6790.discord_framework.modules.core;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.core.commands.info.BotInfoCommand;
import de.timmi6790.discord_framework.modules.core.commands.info.InviteCommand;
import de.timmi6790.discord_framework.modules.core.settings.CommandAutoCorrectSetting;
import de.timmi6790.discord_framework.modules.core.stats.FailedCommandStat;
import de.timmi6790.discord_framework.modules.core.stats.IncorrectArgCommandStat;
import de.timmi6790.discord_framework.modules.core.stats.MissingArgCommandStat;
import de.timmi6790.discord_framework.modules.core.stats.SuccessfulCommandStat;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.stat.StatModule;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class CoreModule extends AbstractModule {
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

        this.getModule(SettingModule.class)
                .ifPresent(settingModule -> {
                            DiscordBot.getLogger().info("Registering settings");
                            settingModule.registerSettings(
                                    new CommandAutoCorrectSetting()

                            );
                        }
                );

        this.getModule(StatModule.class)
                .ifPresent(statModule -> {
                            DiscordBot.getLogger().info("Registering stats");
                            statModule.registerStats(
                                    new FailedCommandStat(),
                                    new MissingArgCommandStat(),
                                    new SuccessfulCommandStat(),
                                    new IncorrectArgCommandStat()
                            );
                        }
                );
    }
}

