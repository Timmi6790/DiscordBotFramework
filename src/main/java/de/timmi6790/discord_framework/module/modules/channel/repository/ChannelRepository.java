package de.timmi6790.discord_framework.module.modules.channel.repository;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;

import java.util.Optional;

/**
 * Channel repository.
 */
public interface ChannelRepository {
    /**
     * Insert a new channel instance into the repository. It is required that the guild already exists inside the
     * repository.
     *
     * @param discordChannelId the discord channel id
     * @param discordGuildID   the discord guild id
     * @return the channel
     */
    ChannelDb create(final long discordChannelId, final long discordGuildID);

    /**
     * Tries to find a channel instance inside the repository with the given discord channel id
     *
     * @param discordChannelId the discord channel id
     * @return the channel
     */
    Optional<ChannelDb> get(final long discordChannelId);
}
