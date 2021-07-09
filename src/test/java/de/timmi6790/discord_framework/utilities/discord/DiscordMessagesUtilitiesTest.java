package de.timmi6790.discord_framework.utilities.discord;

import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscordMessagesUtilitiesTest {
    private static final String EMBEDDED_USER_NAME = "Test";
    private static final String EMBEDDED_USER_URL = "https://cdn.discordapp.com/emojis/754447492845928488.gif?v=1";

    private static final Color EMBEDDED_MEMBER_COLOUR = Color.BLACK;

    private User getEmbedBuilderUser() {
        final User user = mock(User.class);
        when(user.getName()).thenReturn(EMBEDDED_USER_NAME);
        when(user.getEffectiveAvatarUrl()).thenReturn(EMBEDDED_USER_URL);

        return user;
    }

    private Member getEmbedBuilderMember() {
        final Member member = mock(Member.class);
        when(member.getColor()).thenReturn(EMBEDDED_MEMBER_COLOUR);

        return member;
    }


    private void checkEmbedBuilder(final MultiEmbedBuilder multiEmbedBuilder, final boolean fromGuild) {
        assertThat(multiEmbedBuilder.getAuthor().getName()).isEqualTo(EMBEDDED_USER_NAME);
        assertThat(multiEmbedBuilder.getAuthor().getIconUrl()).isEqualTo(EMBEDDED_USER_URL);
        assertThat(multiEmbedBuilder.getColor()).isEqualTo(fromGuild ? EMBEDDED_MEMBER_COLOUR.getRGB() : DiscordMessagesUtilities.DEFAULT_EMBED_COLOUR.getRGB());
    }

    @Test
    void getEmbedBuilderUserAndMemberGuildMessage() {
        final User user = this.getEmbedBuilderUser();
        final Member member = this.getEmbedBuilderMember();
        final MultiEmbedBuilder embedBuilder = DiscordMessagesUtilities.getEmbedBuilder(user, member);
        this.checkEmbedBuilder(embedBuilder, true);
    }

    @Test
    void getEmbedBuilderUserAndMemberPrivateMessage() {
        final User user = this.getEmbedBuilderUser();
        final MultiEmbedBuilder embedBuilder = DiscordMessagesUtilities.getEmbedBuilder(user, null);
        this.checkEmbedBuilder(embedBuilder, false);
    }

    @Test
    void sendPrivateMessageBotCheck() {
        final User user = mock(User.class);
        when(user.isBot()).thenReturn(true);

        assertThat(DiscordMessagesUtilities.sendPrivateMessage(user, new MultiEmbedBuilder())).isFalse();
    }

    @Test
    void sendPrivateMessage() {
        final User user = mock(User.class);
        when(user.isBot()).thenReturn(false);

        final RestAction<PrivateChannel> privateChannel = mock(RestAction.class);
        when(user.openPrivateChannel()).thenReturn(privateChannel);

        assertThat(DiscordMessagesUtilities.sendPrivateMessage(user, new MultiEmbedBuilder().setDescription("A"))).isTrue();
    }
}