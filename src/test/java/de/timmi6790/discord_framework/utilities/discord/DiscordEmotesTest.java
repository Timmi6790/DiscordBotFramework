package de.timmi6790.discord_framework.utilities.discord;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordEmotesTest {
    void checkNumberAgainstEmote(final int number, final DiscordEmotes expectedEmote) {
        assertThat(DiscordEmotes.getNumberEmote(number)).isEqualTo(expectedEmote);
    }

    @Test
    void getNumberEmote() {
        this.checkNumberAgainstEmote(0, DiscordEmotes.ZERO);
        this.checkNumberAgainstEmote(1, DiscordEmotes.ONE);
        this.checkNumberAgainstEmote(2, DiscordEmotes.TWO);
        this.checkNumberAgainstEmote(3, DiscordEmotes.THREE);
        this.checkNumberAgainstEmote(4, DiscordEmotes.FOUR);
        this.checkNumberAgainstEmote(5, DiscordEmotes.FIVE);
        this.checkNumberAgainstEmote(6, DiscordEmotes.SIX);
        this.checkNumberAgainstEmote(7, DiscordEmotes.SEVEN);
        this.checkNumberAgainstEmote(8, DiscordEmotes.EIGHT);
        this.checkNumberAgainstEmote(9, DiscordEmotes.NINE);
        this.checkNumberAgainstEmote(10, DiscordEmotes.TEN);
        this.checkNumberAgainstEmote(-1, DiscordEmotes.ZERO);
    }
}