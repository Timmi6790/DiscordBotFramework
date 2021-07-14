package de.timmi6790.discord_framework.module.modules.command.models;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.utilities.ArgumentUtilities;
import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandParameters {
    public static CommandParameters of(final String rawArgs,
                                       final String[] args,
                                       final boolean guildCommand,
                                       final CommandCause commandCause,
                                       final CommandModule commandModule,
                                       final ChannelDb channelDb,
                                       final UserDb userDb) {
        return new CommandParameters(
                rawArgs,
                args,
                guildCommand,
                commandCause,
                commandModule,
                channelDb,
                userDb
        );
    }

    public static CommandParameters of(final String rawArgs,
                                       final boolean guildCommand,
                                       final CommandCause commandCause,
                                       final CommandModule commandModule,
                                       final ChannelDb channelDb,
                                       final UserDb userDb) {
        final String[] args = ArgumentUtilities.parseRawArguments(rawArgs);
        return new CommandParameters(
                rawArgs,
                args,
                guildCommand,
                commandCause,
                commandModule,
                channelDb,
                userDb
        );
    }

    public static CommandParameters of(final String[] args,
                                       final boolean guildCommand,
                                       final CommandCause commandCause,
                                       final CommandModule commandModule,
                                       final ChannelDb channelDb,
                                       final UserDb userDb) {
        final String rawArgs = String.join(" ", args);
        return new CommandParameters(
                rawArgs,
                args,
                guildCommand,
                commandCause,
                commandModule,
                channelDb,
                userDb
        );
    }

    private final String rawArgs;
    private final String[] args;
    private final boolean guildCommand;
    private final CommandCause commandCause;
    private final CommandModule commandModule;
    private final ChannelDb channelDb;
    private final UserDb userDb;

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
        Checks.check(this.isGuildCommand(), "Can't get guild text channel, for private messages");
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
            return this.getGuild()
                    .getSelfMember()
                    .getPermissions((GuildChannel) this.getGuildTextChannel());
        } else {
            return EnumSet.noneOf(Permission.class);
        }
    }

    public JDA getJda() {
        if (this.isGuildCommand()) {
            return this.getGuild().getJDA();
        } else {
            return this.getUser().getJDA();
        }
    }

    // Messages
    public MultiEmbedBuilder getEmbedBuilder() {
        return DiscordMessagesUtilities.getEmbedBuilder(
                this.getUser(),
                this.isGuildCommand() ? this.getGuildMember() : null
        );
    }

    public void sendMessage(final MultiEmbedBuilder builder) {
        DiscordMessagesUtilities.sendMessage(
                this.getLowestMessageChannel(),
                builder
        );
    }

    public void sendPrivateMessage(final MultiEmbedBuilder builder) {
        DiscordMessagesUtilities.sendPrivateMessage(
                this.getUser(),
                builder
        );
    }

    // Args
    public String getArg(final int argPos) {
        return this.args[argPos];
    }

    public String getArgOrDefault(final int argPos,
                                  final String defaultValue) {
        if (argPos >= this.args.length || this.args[argPos] == null) {
            return defaultValue;
        } else {
            return this.getArg(argPos);
        }
    }
}
