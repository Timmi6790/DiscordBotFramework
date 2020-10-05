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
import net.dv8tion.jda.internal.utils.Checks;

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

    public static CommandParameters of(@Nonnull final MessageReceivedEvent event, @NonNull final String rawArgs) {
        final ModuleManager moduleManager = DiscordBot.getInstance().getModuleManager();

        final long guildId = event.getMessage().isFromGuild() ? event.getMessage().getGuild().getIdLong() : 0;
        final GuildDb guildDb = moduleManager.getModuleOrThrow(GuildDbModule.class).getOrCreate(guildId);

        // Add already obtained elements to cache
        UserDb.getUSER_CACHE().put(event.getAuthor().getIdLong(), event.getAuthor());
        if (event.getMember() != null) {
            guildDb.getMemberCache().put(event.getMember().getIdLong(), event.getMember());
        }

        return new CommandParameters(
                rawArgs,
                SPLITTER.splitToList(rawArgs).toArray(new String[0]),
                event.isFromGuild(),
                CommandCause.USER,
                moduleManager.getModuleOrThrow(ChannelDbModule.class).getOrCreate(event.getChannel().getIdLong(), guildDb.getDiscordId()),
                moduleManager.getModuleOrThrow(UserDbModule.class).getOrCreate(event.getAuthor().getIdLong())
        );
    }

    public static CommandParameters of(@NonNull final CommandParameters commandParameters, @NonNull final String... newArgs) {
        return CommandParameters.of(
                commandParameters,
                commandParameters.getCommandCause(),
                newArgs
        );
    }

    public static CommandParameters of(@NonNull final CommandParameters commandParameters,
                                       @NonNull final CommandCause commandCause,
                                       @NonNull final String... newArgs) {
        return new CommandParameters(
                String.join(" ", newArgs),
                newArgs,
                commandParameters.isGuildCommand(),
                commandCause,
                commandParameters.getChannelDb(),
                commandParameters.getUserDb()
        );
    }

    public CommandParameters(@NonNull final String rawArgs,
                             @NonNull final String[] args,
                             final boolean guildCommand,
                             @NonNull final CommandCause commandCause,
                             @NonNull final ChannelDb channelDb,
                             @NonNull final UserDb userDb) {
        this.rawArgs = rawArgs;
        this.args = args.clone();
        this.guildCommand = guildCommand;
        this.commandCause = commandCause;
        this.channelDb = channelDb;
        this.userDb = userDb;
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

    public MessageChannel getGuildTextChannel() {
        Checks.check(this.isGuildCommand(), "Can't get guild text channel, for dm messages");
        return this.channelDb.getChannel();
    }

    @SneakyThrows
    public PrivateChannel getUserTextChannel() {
        final CompletableFuture<PrivateChannel> futureValue = new CompletableFuture<>();
        this.getUser().openPrivateChannel().queue(futureValue::complete);
        return futureValue.get(1, TimeUnit.MINUTES);
    }

    /**
     * Returns the guild channel if it is send from a guild, otherwise it will return the user channel
     *
     * @return the lowest message channel
     */
    public MessageChannel getLowestMessageChannel() {
        if (this.isGuildCommand()) {
            return this.getGuildTextChannel();
        } else {
            return this.getUserTextChannel();
        }
    }

    public Set<Permission> getDiscordPermissions() {
        if (this.isGuildCommand()) {
            return this.channelDb.getGuildDb().getGuild().getSelfMember().getPermissions((GuildChannel) this.getGuildTextChannel());
        } else {
            return EnumSet.noneOf(Permission.class);
        }
    }
}
