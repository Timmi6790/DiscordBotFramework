package de.timmi6790.discord_framework.modules.channel.repository;

import de.timmi6790.discord_framework.modules.channel.ChannelDb;

import java.util.Optional;

public interface ChannelRepository {
    ChannelDb create(final long discordId, final long guildId);

    Optional<ChannelDb> get(final long discordId);
}
