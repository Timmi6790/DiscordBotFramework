package de.timmi6790.statsbotdiscord.modules.core;

import de.timmi6790.statsbotdiscord.AbstractModule;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.core.commands.info.CommandAbout;
import de.timmi6790.statsbotdiscord.modules.core.commands.info.CommandHelp;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java.JavaPlayerStatsCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class CoreModule extends AbstractModule {
    public CoreModule() {
        super("Core");
    }

    @Override
    public void onEnable() {
        this.registerDatabaseMappings();
        StatsBot.getCommandManager().registerCommands(
                new CommandHelp(),
                new CommandAbout(),
                new JavaPlayerStatsCommand()
        );
    }

    @Override
    public void onDisable() {

    }

    private void registerDatabaseMappings() {
        StatsBot.getDatabase().registerRowMapper(Channel.class, (rs, ctx) -> {
            if (rs.getInt("id") == 0) {
                return null;
            }

            return new Channel(rs.getInt("id"), rs.getLong("discordId"),
                    Guild.getOrCreate(rs.getLong("serverDiscordId")), rs.getBoolean("disabled"));

        }).registerRowMapper(Guild.class, (rs, ctx) -> {
            if (rs.getInt("id") == 0) {
                return null;
            }

            final String aliases = rs.getString("aliases");
            String[] aliasList = aliases == null ? new String[]{} : aliases.split(",");

            return new Guild(rs.getInt("id"), rs.getLong("discordId"), rs.getBoolean("banned"),
                    new HashSet<>(Arrays.asList(aliasList)), null);

        }).registerRowMapper(User.class, (rs, ctx) -> {
            if (rs.getInt("id") == 0) {
                return null;
            }

            final String perms = rs.getString("perms");
            final String[] permList = perms == null ? new String[]{} : perms.split(",");

            return new User(rs.getInt("id"), rs.getLong("discordId"), null, new ArrayList<>(),
                    rs.getBoolean("banned"), rs.getLong("shopPoints"),
                    new ArrayList<>(Arrays.asList(permList)), new ArrayList<>());
        });
    }
}
