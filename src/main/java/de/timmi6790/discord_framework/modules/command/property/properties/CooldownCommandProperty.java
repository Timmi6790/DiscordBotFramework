package de.timmi6790.discord_framework.modules.command.property.properties;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.property.CommandProperty;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.NonNull;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.concurrent.TimeUnit;

public class CooldownCommandProperty implements CommandProperty<Boolean> {
    private final Cache<Long, Long> cooldownCache;
    private final TimeUnit timeUnit;
    private final long duration;

    public CooldownCommandProperty(final long duration, final TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        this.duration = duration;

        this.cooldownCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(duration, timeUnit)
                .build();
    }

    private String getFormattedCooldownTime(final long timeLeft) {
        final long hours = timeLeft / 3600;
        final long minutes = (timeLeft % 3600) / 60;
        final long seconds = timeLeft % 60;

        final StringBuilder stringBuilder = new StringBuilder();
        if (hours != 0) {
            stringBuilder.append(String.format("%02d hours ", hours));
        }

        if (minutes != 0) {
            stringBuilder.append(String.format("%02d minutes ", minutes));
        }

        if (seconds != 0) {
            stringBuilder.append(String.format("%02d seconds", seconds));
        }

        return stringBuilder.toString();
    }

    private void sendErrorMessage(final CommandParameters commandParameters, final long cooldownTill) {
        final String timeLeft = this.getFormattedCooldownTime(cooldownTill - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("On Cooldown")
                        .appendDescription(
                                "Please wait %s before using the command again.",
                                MarkdownUtil.monospace(timeLeft)
                        ),
                250
        );
    }

    @Override
    public Boolean getValue() {
        return Boolean.TRUE;
    }

    @Override
    public boolean onCommandExecution(@NonNull final AbstractCommand command,
                                      @NonNull final CommandParameters commandParameters) {
        final long userId = commandParameters.getUserDb().getDiscordId();

        final Long cooldownTill = this.cooldownCache.getIfPresent(userId);
        if (cooldownTill != null) {
            this.sendErrorMessage(commandParameters, cooldownTill);
            return false;
        }

        this.cooldownCache.put(
                userId,
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + this.timeUnit.toSeconds(this.duration)
        );
        return true;
    }
}
