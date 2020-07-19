package de.timmi6790.statsbotdiscord.modules.command;

import de.timmi6790.statsbotdiscord.datatypes.MapBuilder;
import de.timmi6790.statsbotdiscord.events.MessageReceivedIntEvent;
import de.timmi6790.statsbotdiscord.modules.core.ChannelDb;
import de.timmi6790.statsbotdiscord.modules.core.GuildDb;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.collections.map.HashedMap;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

@Data
@AllArgsConstructor
public class CommandParameters {
    private final EnumSet<Permission> discordChannelPermissions;
    private final ChannelDb channelDb;
    private final UserDb userDb;
    private final MessageReceivedIntEvent event;
    private String[] args;

    public CommandParameters(final CommandParameters commandParameters) {
        this.discordChannelPermissions = commandParameters.discordChannelPermissions;
        this.channelDb = commandParameters.channelDb;
        this.userDb = commandParameters.userDb;
        this.event = commandParameters.event;

        this.args = new String[commandParameters.args.length];
        System.arraycopy(commandParameters.args, 0, this.args, 0, this.args.length);
    }

    public boolean isFromGuild() {
        return this.event.isFromGuild();
    }

    public GuildDb getServer() {
        return this.channelDb.getGuildDb();
    }

    public MessageChannel getDiscordChannel() {
        return this.event.getMessage().getChannel();
    }

    public Map<String, String> getSentryMap() {
        return new MapBuilder<String, String>(HashedMap::new)
                .put("channelId", String.valueOf(this.channelDb.getDatabaseId()))
                .put("userId", String.valueOf(this.userDb.getDatabaseId()))
                .put("args", Arrays.toString(this.args))
                .build();
    }
}
