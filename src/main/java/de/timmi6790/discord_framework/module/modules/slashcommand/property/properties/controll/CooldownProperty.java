package de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.controll;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.SlashCommandProperty;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class CooldownProperty implements SlashCommandProperty<Boolean> {
    private final Cache<Long, Long> cache;
    private final int cooldownInSeconds;

    public CooldownProperty(final long duration, final TimeUnit timeUnit) {
        if (duration <= 0) {
            throw new IllegalArgumentException("The duration must be bigger than 0");
        }

        this.cooldownInSeconds = (int) timeUnit.toSeconds(duration);
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(duration, timeUnit)
                .build();
    }

    private long getCurrentTimeSeconds() {
        final long currentTimeMillis = System.currentTimeMillis();
        return TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis);
    }

    protected String getFormattedTime(final long timeLeft) {
        final StringJoiner stringBuilder = new StringJoiner(" ");
        final long hours = timeLeft / 3600;
        if (hours != 0) {
            stringBuilder.add(
                    String.format(
                            "%2d hours",
                            hours
                    )
            );
        }

        final long minutes = (timeLeft % 3600) / 60;
        if (minutes != 0) {
            stringBuilder.add(
                    String.format(
                            "%2d minutes",
                            minutes
                    )
            );
        }

        final long seconds = timeLeft % 60;
        if (seconds != 0) {
            stringBuilder.add(
                    String.format(
                            "%2d seconds",
                            seconds
                    )
            );
        }

        return stringBuilder.toString().trim();
    }

    protected void sendErrorMessage(final SlashCommandParameters commandParameters, final long cooldownTill) {
        final long currentTimeSeconds = this.getCurrentTimeSeconds();
        final String timeLeft = this.getFormattedTime(cooldownTill - currentTimeSeconds);
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("On Cooldown")
                        .appendDescription(
                                "Please wait %s before using the command again.",
                                MarkdownUtil.monospace(timeLeft)
                        )
        );
    }

    @Override
    public Boolean getValue() {
        return Boolean.TRUE;
    }

    @Override
    public boolean onCommandExecution(final SlashCommand command, final SlashCommandParameters commandParameters) {
        final long userId = commandParameters.getUserDb().getDiscordId();

        final Long cooldownTill = this.cache.getIfPresent(userId);
        if (cooldownTill != null) {
            this.sendErrorMessage(commandParameters, cooldownTill);
            return false;
        }

        final long currentTimeSeconds = this.getCurrentTimeSeconds();
        this.cache.put(
                userId,
                currentTimeSeconds + this.cooldownInSeconds
        );
        return true;
    }
}
