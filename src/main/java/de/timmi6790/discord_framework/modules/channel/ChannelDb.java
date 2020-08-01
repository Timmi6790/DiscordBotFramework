package de.timmi6790.discord_framework.modules.channel;

import de.timmi6790.discord_framework.modules.guild.GuildDb;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChannelDb {
    private final int databaseId;
    private final long discordId;

    private final GuildDb guildDb;

    private boolean disabled;
}
