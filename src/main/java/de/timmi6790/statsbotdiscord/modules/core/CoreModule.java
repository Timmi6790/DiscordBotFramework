package de.timmi6790.statsbotdiscord.modules.core;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.AbstractModule;
import de.timmi6790.statsbotdiscord.modules.core.achievements.CommandAutoCorrectAchievement;
import de.timmi6790.statsbotdiscord.modules.core.commands.info.*;
import de.timmi6790.statsbotdiscord.modules.core.commands.management.BotInfoCommand;
import de.timmi6790.statsbotdiscord.modules.core.commands.management.UserInfoCommand;
import de.timmi6790.statsbotdiscord.modules.core.settings.CommandAutoCorrectSetting;
import de.timmi6790.statsbotdiscord.modules.core.stats.FailedCommandStat;
import de.timmi6790.statsbotdiscord.modules.core.stats.IncorrectArgCommandStat;
import de.timmi6790.statsbotdiscord.modules.core.stats.MissingArgCommandStat;
import de.timmi6790.statsbotdiscord.modules.core.stats.SuccessfulCommandStat;

import java.util.*;
import java.util.stream.Collectors;

public class CoreModule extends AbstractModule {
    public CoreModule() {
        super("Core");
    }

    @Override
    public void onEnable() {
        this.registerDatabaseMappings();
        StatsBot.getCommandManager().registerCommands(
                new HelpCommand(),
                new AboutCommand(),
                new BotInfoCommand(),
                new UserInfoCommand(),
                new InviteCommand(),
                new AccountDeletionCommand(),
                new SettingsCommand()
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
                new CommandAutoCorrectAchievement()
        );
    }

    @Override
    public void onDisable() {

    }

    private void registerDatabaseMappings() {
        StatsBot.getDatabase().registerRowMapper(ChannelDb.class, (rs, ctx) -> {
            if (rs.getInt("id") == 0) {
                return null;
            }

            return new ChannelDb(rs.getInt("id"), rs.getLong("discordId"),
                    GuildDb.getOrCreate(rs.getLong("serverDiscordId")), rs.getBoolean("disabled"));

        }).registerRowMapper(GuildDb.class, (rs, ctx) -> {
            if (rs.getInt("id") == 0) {
                return null;
            }

            final String aliases = rs.getString("aliases");
            final String[] aliasList = aliases == null ? new String[]{} : aliases.split(",");

            return new GuildDb(rs.getInt("id"), rs.getLong("discordId"), rs.getBoolean("banned"),
                    new HashSet<>(Arrays.asList(aliasList)), null);

        }).registerRowMapper(UserDb.class, (rs, ctx) -> {
            if (rs.getInt("id") == 0) {
                return null;
            }

            final Set<String> permList = Optional.ofNullable(rs.getString("perms"))
                    .map(perms -> Arrays.stream(perms.split(",")).collect(Collectors.toSet()))
                    .orElse(new HashSet<>());

            final Map<Integer, String> settings = Optional.ofNullable(rs.getString("settings"))
                    .filter(value -> !value.isEmpty())
                    .map(value -> Arrays.stream(value.split(";"))
                            .map(setting -> setting.split(","))
                            .filter(values -> values.length == 2)
                            .collect(Collectors.toMap(values -> Integer.parseInt(values[0]), values -> values[1])))
                    .orElse(new HashMap<>());

            final Map<Integer, Integer> stats = Optional.ofNullable(rs.getString("stats"))
                    .filter(value -> !value.isEmpty())
                    .map(value -> Arrays.stream(value.split(";"))
                            .map(setting -> setting.split(","))
                            .filter(values -> values.length == 2)
                            .collect(Collectors.toMap(values -> Integer.parseInt(values[0]), values -> Integer.parseInt(values[1]))))
                    .orElse(new HashMap<>());

            final Set<Integer> achievementList = Optional.ofNullable(rs.getString("achievements"))
                    .map(achievements -> Arrays.stream(achievements.split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toSet()))
                    .orElse(new HashSet<>());

            return new UserDb(rs.getInt("id"), rs.getLong("discordId"), null, new ArrayList<>(),
                    rs.getBoolean("banned"), rs.getLong("shopPoints"), permList, settings, stats, achievementList);
        });
    }
}

