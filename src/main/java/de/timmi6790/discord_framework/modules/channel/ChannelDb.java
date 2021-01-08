package de.timmi6790.discord_framework.modules.channel;

import de.timmi6790.discord_framework.modules.guild.GuildDb;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * Repository instance of a discord channel, used in all custom bot logics as a reference to the discord channel.
 */
@Data
@EqualsAndHashCode(exclude = {"discord"})
@ToString(exclude = {"discord"})
public class ChannelDb {
    private final int repositoryId;
    private final long discordId;
    private final GuildDb guildDb;
    private boolean disabled;

    private final ShardManager discord;

    /**
     * Instantiates a new ChannelDb.
     *
     * @param guildDb      the guildDb
     * @param discord      the discord instance
     * @param repositoryId the repository id
     * @param discordId    the discord id
     * @param disabled     is channel disabled
     */
    public ChannelDb(final GuildDb guildDb,
                     final ShardManager discord,
                     final int repositoryId,
                     final long discordId,
                     final boolean disabled) {
        this.repositoryId = repositoryId;
        this.discordId = discordId;
        this.disabled = disabled;

        this.guildDb = guildDb;
        this.discord = discord;
    }

    /**
     * Gets the corresponding discord channel for this instance. This might be retrieved if not cached
     *
     * @return the discord channel
     */
    public MessageChannel getChannel() {
        return this.discord.getTextChannelById(this.discordId);
    }
}
