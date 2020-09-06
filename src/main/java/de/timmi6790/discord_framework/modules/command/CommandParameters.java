package de.timmi6790.discord_framework.modules.command;

import com.google.common.base.Splitter;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Data
public class CommandParameters {
    private static final Splitter SPLITTER = Splitter.on(' ')
            .trimResults()
            .omitEmptyStrings();

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

    public CommandParameters(@Nonnull final MessageReceivedEvent event, final String rawArgs) {
        final ModuleManager moduleManager = DiscordBot.getInstance().getModuleManager();

        final long guildId = event.getMessage().isFromGuild() ? event.getMessage().getGuild().getIdLong() : 0;
        final GuildDb guildDb = moduleManager.getModuleOrThrow(GuildDbModule.class).getOrCreate(guildId);
        
        // Add already obtained elements to cache
        UserDb.getUserCache().put(event.getAuthor().getIdLong(), event.getAuthor());
        if (event.getMember() != null) {
            guildDb.getMemberCache().put(event.getMember().getIdLong(), event.getMember());
        }

        this.rawArgs = rawArgs;
        this.args = SPLITTER.splitToList(rawArgs).toArray(new String[0]);
        this.guildCommand = event.isFromGuild();
        this.commandCause = CommandCause.USER;
        this.channelDb = moduleManager.getModuleOrThrow(ChannelDbModule.class).getOrCreate(event.getChannel().getIdLong(), guildDb.getDiscordId());
        this.userDb = moduleManager.getModuleOrThrow(UserDbModule.class).getOrCreate(event.getAuthor().getIdLong());
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
