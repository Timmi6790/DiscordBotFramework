package de.timmi6790.statsbotdiscord.modules.core;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.AbstractModule;
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

public class CoreModule extends AbstractModule {
    public CoreModule() {
        super("Core");
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

        StatsBot.getSettingManager().registerSettings(
                new CommandAutoCorrectSetting()
        );

        StatsBot.getStatManager().registerStats(
                new FailedCommandStat(),
                new MissingArgCommandStat(),
                new SuccessfulCommandStat(),
                new IncorrectArgCommandStat()
        );

        StatsBot.getAchievementManager().registerAchievements(
                // new CommandAutoCorrectAchievement()
        );
    }

    @Override
    public void onDisable() {

    }
}

