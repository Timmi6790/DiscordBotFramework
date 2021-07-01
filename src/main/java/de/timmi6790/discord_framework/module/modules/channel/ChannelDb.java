package de.timmi6790.discord_framework.module.modules.channel;

import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * Repository instance of a discord channel, used in all custom bot logics as a reference to the discord channel.
 */
@Data
public class ChannelDb {
    private final long discordId;
    private final GuildDb guildDb;
    private boolean disabled;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final ShardManager discord;

    /**
     * Instantiates a new ChannelDb.
     *
     * @param guildDb   the guildDb
     * @param discord   the discord instance
     * @param discordId the discord id
     * @param disabled  is channel disabled
     */
    public ChannelDb(final GuildDb guildDb,
                     final ShardManager discord,
                     final long discordId,
                     final boolean disabled) {
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
