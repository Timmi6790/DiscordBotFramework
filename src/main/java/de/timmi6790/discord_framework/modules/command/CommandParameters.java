package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.EnumSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class CommandParameters {
    private final String rawArgs;
    private final String[] args;
    private final boolean guildCommand;
    private final CommandCause commandCause;
    private final ChannelDb channelDb;
    private final UserDb userDb;

    public CommandParameters(final CommandParameters commandParameters, final String... newArgs) {
        this.rawArgs = String.join(" ", newArgs);
        this.args = newArgs;
        this.guildCommand = commandParameters.isGuildCommand();
        this.commandCause = commandParameters.commandCause;
        this.channelDb = commandParameters.getChannelDb();
        this.userDb = commandParameters.getUserDb();
    }

    public CommandParameters(final CommandParameters commandParameters, final CommandCause commandCause, final String... newArgs) {
        this.rawArgs = String.join(" ", newArgs);
        this.args = newArgs;
        this.guildCommand = commandParameters.isGuildCommand();
        this.commandCause = commandParameters.commandCause;
        this.channelDb = commandParameters.getChannelDb();
        this.userDb = commandParameters.getUserDb();
    }

    public User getUser() {
        return this.userDb.getUser();
    }

    public Guild getGuild() {
        return this.getGuildDb().getGuild();
    }

    public GuildDb getGuildDb() {
        return this.channelDb.getGuildDb();
    }

    public MessageChannel getTextChannel() {
        if (this.isGuildCommand()) {
            return this.channelDb.getChannel();
        } else {
            return this.getUser().openPrivateChannel().complete();
        }
    }

    public Set<Permission> getDiscordPermissions() {
        if (this.isGuildCommand()) {
            return this.channelDb.getGuildDb().getGuild().getSelfMember().getPermissions((GuildChannel) this.getTextChannel());
        } else {
            return EnumSet.noneOf(Permission.class);
        }
    }
}
