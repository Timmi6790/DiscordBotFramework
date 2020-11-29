package de.timmi6790.discord_framework.modules.guild.repository;

import de.timmi6790.discord_framework.modules.guild.GuildDb;

import java.util.Optional;

public interface GuildDbRepository {
    GuildDb create(final long discordId);

    Optional<GuildDb> get(final long discordId);

    void updateSetting(int guildDatabaseId, int settingId, String newValue);
}
