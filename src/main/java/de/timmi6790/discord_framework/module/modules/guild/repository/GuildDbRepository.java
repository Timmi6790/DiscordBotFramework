package de.timmi6790.discord_framework.module.modules.guild.repository;

import de.timmi6790.discord_framework.module.modules.guild.GuildDb;

import java.util.Optional;

public interface GuildDbRepository {
    GuildDb create(final long discordId);

    Optional<GuildDb> get(final long discordId);
}
