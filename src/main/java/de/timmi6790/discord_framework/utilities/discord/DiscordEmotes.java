package de.timmi6790.discord_framework.utilities.discord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
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

    private final String emote;

    public static DiscordEmotes getNumberEmote(final int number) {
        return switch (number) {
            case 1 -> ONE;
            case 2 -> TWO;
            case 3 -> THREE;
            case 4 -> FOUR;
            case 5 -> FIVE;
            case 6 -> SIX;
            case 7 -> SEVEN;
            case 8 -> EIGHT;
            case 9 -> NINE;
            case 10 -> TEN;
            default -> ZERO;
        };
    }
}
