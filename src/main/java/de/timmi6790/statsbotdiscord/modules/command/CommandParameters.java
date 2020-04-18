package de.timmi6790.statsbotdiscord.modules.command;

import de.timmi6790.statsbotdiscord.events.EventMessageReceived;
import de.timmi6790.statsbotdiscord.modules.core.Channel;
import de.timmi6790.statsbotdiscord.modules.core.Guild;
import de.timmi6790.statsbotdiscord.modules.core.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class CommandParameters implements Cloneable {
    private final EnumSet<Permission> discordChannelPermissions;
    private final Channel channel;
    private final User user;
    private String[] args;
    private final EventMessageReceived event;

    public Guild getServer() {
        return this.channel.getGuild();
    }

    public MessageChannel getDiscordChannel() {
        return this.event.getMessage().getChannel();
    }

    public Map<String, String> getSentryMap() {
        final Map<String, String> sentryMap = new HashMap<>();

        sentryMap.put("channelId", String.valueOf(this.channel.getDatabaseId()));
        sentryMap.put("userId", String.valueOf(this.user.getDatabaseId()));
        sentryMap.put("args", Arrays.toString(this.args));

        return sentryMap;
    }

    @SneakyThrows
    @Override
    public CommandParameters clone() {
        return (CommandParameters) super.clone();
    }
}
