package de.timmi6790.discord_framework.modules.command;

import com.google.common.base.Splitter;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.guild.GuildDb;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
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
import java.util.function.Consumer;

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

    public CommandParameters(@Nonnull final MessageReceivedEvent event, @NonNull final String rawArgs) {
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

    public void sendMessage(@NonNull final MultiEmbedBuilder multiEmbedBuilder, @NonNull final Consumer<Message> success) {
        DiscordMessagesUtilities.sendMessage(this.getLowestMessageChannel(), multiEmbedBuilder, success);
    }

    public void sendMessage(@NonNull final MultiEmbedBuilder multiEmbedBuilder) {
        DiscordMessagesUtilities.sendMessage(this.getLowestMessageChannel(), multiEmbedBuilder);
    }
}
