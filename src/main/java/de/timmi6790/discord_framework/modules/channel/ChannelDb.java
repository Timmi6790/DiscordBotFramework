package de.timmi6790.discord_framework.modules.channel;

import de.timmi6790.discord_framework.modules.guild.GuildDb;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;

@Data
@EqualsAndHashCode(exclude = {"discord"})
@ToString(exclude = {"discord"})
public class ChannelDb {
    private final int databaseId;
    private final long discordId;
    private final GuildDb guildDb;
    private boolean disabled;

    private final JDA discord;

    public ChannelDb(final GuildDb guildDb,
                     final JDA discord,
                     final int databaseId,
                     final long discordId,
                     final boolean disabled) {
        this.databaseId = databaseId;
        this.discordId = discordId;
        this.disabled = disabled;

        this.guildDb = guildDb;
        this.discord = discord;
    }

    public MessageChannel getChannel() {
        return this.discord.getTextChannelById(this.discordId);
    }
}
