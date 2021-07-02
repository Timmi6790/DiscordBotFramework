package de.timmi6790.discord_framework.module.modules.command_old;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.EnumSet;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

class CommandParametersTest {
    @Test
    void ofNewArgs() {
        final UserDb userDb = Mockito.mock(UserDb.class);
        final ChannelDb channelDb = Mockito.mock(ChannelDb.class);
        final CommandParameters commandParameters = Mockito.spy(new CommandParameters(
                "",
                new String[]{},
                true,
                CommandCause.USER,
                channelDb,
                userDb
        ));

        final String[] newArgs = {"a", "d", "c"};

        final CommandParameters newCommandParameters = CommandParameters.of(commandParameters, newArgs);
        assertThat(newCommandParameters.getArgs()).isEqualTo(newArgs);
    }

    @Test
    void ofNewArgsAndCommandCause() {
        final UserDb userDb = Mockito.mock(UserDb.class);
        final ChannelDb channelDb = Mockito.mock(ChannelDb.class);
        final CommandParameters commandParameters = Mockito.spy(new CommandParameters(
                "",
                new String[]{},
                true,
                CommandCause.USER,
                channelDb,
                userDb
        ));

        final String[] newArgs = {"a", "d", "c"};
        final CommandCause newCause = CommandCause.EMOTES;

        final CommandParameters newCommandParameters = CommandParameters.of(commandParameters, newCause, newArgs);
        assertThat(newCommandParameters.getArgs()).isEqualTo(newArgs);
        assertThat(newCommandParameters.getCommandCause()).isEqualTo(newCause);
    }

    @Test
    void getArgs() {
        final ChannelDb channelDb = Mockito.mock(ChannelDb.class);
        final UserDb userDb = Mockito.mock(UserDb.class);

        final String[] args = {"a", "b", "c", "d", "0"};

        final CommandParameters commandParameters = Mockito.spy(new CommandParameters(
                "",
                args,
                true,
                CommandCause.USER,
                channelDb,
                userDb
        ));

        assertThat(commandParameters.getArgs()).isEqualTo(args);
    }

    @Test
    void getLowestMessageChannelPrivateMessage() {
        final CommandParameters commandParameters = Mockito.mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(false);
        when(commandParameters.getLowestMessageChannel()).thenCallRealMethod();

        final PrivateChannel privateChannel = Mockito.mock(PrivateChannel.class);
        when(commandParameters.getUserTextChannel()).thenReturn(privateChannel);

        assertThat(commandParameters.getLowestMessageChannel()).isEqualTo(privateChannel);
    }

    @Test
    void getLowestMessageChannelGuildMessage() {
        final CommandParameters commandParameters = Mockito.mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(true);
        when(commandParameters.getLowestMessageChannel()).thenCallRealMethod();

        final MessageChannel messageChannel = Mockito.mock(MessageChannel.class);
        when(commandParameters.getGuildTextChannel()).thenReturn(messageChannel);

        assertThat(commandParameters.getLowestMessageChannel()).isEqualTo(messageChannel);
    }

    @Test
    void getDiscordPermissionsPrivateMessage() {
        final CommandParameters commandParameters = Mockito.mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(false);
        when(commandParameters.getDiscordPermissions()).thenCallRealMethod();

        assertThat(commandParameters.getDiscordPermissions()).isEmpty();
    }

    @Test
    void getDiscordPermissionsGuildMessage() {
        final EnumSet<Permission> permissions = EnumSet.allOf(Permission.class);

        final Member member = Mockito.mock(Member.class);
        when(member.getPermissions(Mockito.any())).thenReturn(permissions);

        final Guild guild = Mockito.mock(Guild.class);
        when(guild.getSelfMember()).thenReturn(member);

        final GuildDb guildDb = Mockito.mock(GuildDb.class);
        when(guildDb.getGuild()).thenReturn(guild);

        final ChannelDb channelDb = Mockito.mock(ChannelDb.class);
        when(channelDb.getGuildDb()).thenReturn(guildDb);

        when(channelDb.getGuildDb().getGuild().getSelfMember().getPermissions(Mockito.any())).thenReturn(permissions);

        final UserDb userDb = Mockito.mock(UserDb.class);
        final CommandParameters commandParameters = Mockito.spy(new CommandParameters(
                "",
                new String[]{},
                true,
                CommandCause.USER,
                channelDb,
                userDb
        ));

        assertThat(commandParameters.getDiscordPermissions()).containsExactly(permissions.toArray(new Permission[0]));
    }
}