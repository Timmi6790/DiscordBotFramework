package de.timmi6790.discord_framework.module.modules.command.models;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CommandParametersTest {
    private CommandParameters createCommandParameters(final ChannelDb channelDb, final UserDb userDb) {
        return spy(
                CommandParameters.of(
                        "test",
                        true,
                        BaseCommandCause.MESSAGE,
                        channelDb,
                        userDb
                )
        );
    }

    private CommandParameters createCommandParameters(final String[] args) {
        return spy(
                CommandParameters.of(
                        args,
                        true,
                        BaseCommandCause.MESSAGE,
                        mock(ChannelDb.class),
                        mock(UserDb.class)
                )
        );
    }

    @Test
    void getUser() {
        final User expected = mock(User.class);

        final UserDb userDb = mock(UserDb.class);
        when(userDb.getUser()).thenReturn(expected);
        final ChannelDb channelDb = mock(ChannelDb.class);
        final CommandParameters commandParameters = this.createCommandParameters(channelDb, userDb);

        final User result = commandParameters.getUser();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getGuild() {
        final Guild expected = mock(Guild.class);

        final GuildDb guildDb = mock(GuildDb.class);
        when(guildDb.getGuild()).thenReturn(expected);

        final UserDb userDb = mock(UserDb.class);

        final ChannelDb channelDb = mock(ChannelDb.class);
        when(channelDb.getGuildDb()).thenReturn(guildDb);

        final CommandParameters commandParameters = this.createCommandParameters(channelDb, userDb);

        final Guild result = commandParameters.getGuild();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getGuildDb() {
        final GuildDb expected = mock(GuildDb.class);

        final UserDb userDb = mock(UserDb.class);

        final ChannelDb channelDb = mock(ChannelDb.class);
        when(channelDb.getGuildDb()).thenReturn(expected);

        final CommandParameters commandParameters = this.createCommandParameters(channelDb, userDb);

        final GuildDb result = commandParameters.getGuildDb();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getArgs() {
        final String[] expected = new String[]{"test", "test2", "test3"};
        final CommandParameters commandParameters = this.createCommandParameters(expected);

        final String[] result = commandParameters.getArgs();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getGuildTextChannel_is_guild() {
        final MessageChannel expected = mock(MessageChannel.class);

        final ChannelDb channelDb = mock(ChannelDb.class);
        when(channelDb.getChannel()).thenReturn(expected);
        final CommandParameters commandParameters = CommandParameters.of(
                "test",
                true,
                BaseCommandCause.MESSAGE,
                channelDb,
                mock(UserDb.class)
        );

        final MessageChannel result = commandParameters.getGuildTextChannel();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getGuildTextChannel_exception() {
        final ChannelDb channelDb = mock(ChannelDb.class);
        final CommandParameters commandParameters = CommandParameters.of(
                "test",
                false,
                BaseCommandCause.MESSAGE,
                channelDb,
                mock(UserDb.class)
        );

        assertThrows(
                IllegalArgumentException.class,
                commandParameters::getGuildTextChannel
        );
    }

    @Test
    void getArgOrDefault_arg() {
        final String expected = "test";
        final String[] args = new String[]{expected};

        final CommandParameters commandParameters = this.createCommandParameters(args);
        final String result = commandParameters.getArgOrDefault(0, "default");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getArgOrDefault_default() {
        final String[] args = new String[]{"test"};
        final String expected = "default";

        final CommandParameters commandParameters = this.createCommandParameters(args);
        final String result = commandParameters.getArgOrDefault(1, expected);
        assertThat(result).isEqualTo(expected);
    }
}