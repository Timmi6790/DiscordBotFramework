package de.timmi6790.statsbotdiscord.utilities.discord;

import lombok.Getter;

public enum DiscordEmotes {
    ZERO("0️⃣"),
    ONE("1️⃣"),
    TWO("2️⃣"),
    THREE("3️⃣"),
    FOUR("4️⃣"),
    FIVE("5️⃣"),
    SIX("6️⃣"),
    SEVEN("7️⃣"),
    EIGHT("8️⃣"),
    NINE("9️⃣"),
    TEN("🔟"),
    LEFT_ARROW("◀"),
    FAR_LEFT_ARROW("⏪"),
    RIGHT_ARROW("▶"),
    FAR_RIGHT_ARROW("⏩"),
    CHECK_MARK("✅"),
    RED_CROSS_MARK("❌"),
    FOLDER("\uD83D\uDCC1");

    @Getter
    private final String emote;

    DiscordEmotes(final String emote) {
        this.emote = emote;
    }

    public static DiscordEmotes getNumberEmote(final int number) {
        switch (number) {
            case 1:
                return ONE;
            case 2:
                return TWO;
            case 3:
                return THREE;
            case 4:
                return FOUR;
            case 5:
                return FIVE;
            case 6:
                return SIX;
            case 7:
                return SEVEN;
            case 8:
                return EIGHT;
            case 9:
                return NINE;
            case 10:
                return TEN;
            default:
                return ZERO;
        }
    }
}
