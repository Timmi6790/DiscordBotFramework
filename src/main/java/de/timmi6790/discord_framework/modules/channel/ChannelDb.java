package de.timmi6790.discord_framework.modules.channel;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import lombok.Data;
import net.dv8tion.jda.api.entities.MessageChannel;

@Data
public class ChannelDb {
    private final int databaseId;
    private final long discordId;

    private final GuildDb guildDb;

    private boolean disabled;

    public ChannelDb(final int databaseId, final long discordId, final GuildDb guildDb, final boolean disabled) {
        this.databaseId = databaseId;
        this.discordId = discordId;
        this.guildDb = guildDb;
        this.disabled = disabled;
    }

    public MessageChannel getChannel() {
        return DiscordBot.getInstance().getDiscord().getTextChannelById(this.discordId);
    }
}
