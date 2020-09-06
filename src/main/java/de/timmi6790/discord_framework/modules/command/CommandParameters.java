package de.timmi6790.discord_framework.modules.command;

import de.timmi6790.discord_framework.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Data
public class CommandParameters {
    private final String rawArgs;
    private final String[] args;
    private final boolean guildCommand;
    private final CommandCause commandCause;
    private final ChannelDb channelDb;
    private final UserDb userDb;

    public CommandParameters(@NonNull final String rawArgs, @NonNull final String[] args, final boolean guildCommand, @NonNull final CommandCause commandCause,
                             @NonNull final ChannelDb channelDb, @NonNull final UserDb userDb) {
        this.rawArgs = rawArgs;
        this.args = args.clone();
        this.guildCommand = guildCommand;
        this.commandCause = commandCause;
        this.channelDb = channelDb;
        this.userDb = userDb;
    }

    public CommandParameters(@NonNull final CommandParameters commandParameters, @NonNull final String... newArgs) {
        this.rawArgs = String.join(" ", newArgs);
        this.args = newArgs.clone();
        this.guildCommand = commandParameters.isGuildCommand();
        this.commandCause = commandParameters.commandCause;
        this.channelDb = commandParameters.getChannelDb();
        this.userDb = commandParameters.getUserDb();
    }

    public CommandParameters(@NonNull final CommandParameters commandParameters, @NonNull final CommandCause commandCause, @NonNull final String... newArgs) {
        this.rawArgs = String.join(" ", newArgs);
        this.args = newArgs.clone();
        this.guildCommand = commandParameters.isGuildCommand();
        this.commandCause = commandCause;
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

    public Member getGuildMember() {
        return this.getGuildDb().getMember(this.getUser());
    }

    public String[] getArgs() {
        return this.args.clone();
    }

    @SneakyThrows
    public MessageChannel getTextChannel() {
        if (this.isGuildCommand()) {
            return this.channelDb.getChannel();
        } else {
            final CompletableFuture<TextChannel> futureValue = new CompletableFuture<>();
            this.getUser().openPrivateChannel().queue(privateChannel -> futureValue.complete((TextChannel) privateChannel));
            return futureValue.get(1, TimeUnit.MINUTES);
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
