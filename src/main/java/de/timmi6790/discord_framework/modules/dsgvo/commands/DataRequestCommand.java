package de.timmi6790.discord_framework.modules.dsgvo.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.dsgvo.DsgvoModule;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class DataRequestCommand extends AbstractCommand {
    private final Cache<Long, Long> cooldownCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();

    private final DsgvoModule dsgvoModule;

    public DataRequestCommand() {
        super("giveMeMyData", "Info", "Get all my data!", "");

        this.dsgvoModule = getModuleManager().getModuleOrThrow(DsgvoModule.class);
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
            stringBuilder.append(String.format("%02d minutes", minutes));
        }

        if (seconds != 0) {
            stringBuilder.append(String.format("%02d seconds", seconds));
        }

        return stringBuilder.toString();
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final long userId = commandParameters.getUserDb().getDiscordId();
        final Long lastCommandUse = this.cooldownCache.getIfPresent(userId);
        if (lastCommandUse != null) {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("On Cooldown")
                            .appendDescription(
                                    "You can request your personal data once per day.\n" +
                                            "Please wait %s before using the command again.",
                                    MarkdownUtil.monospace(
                                            this.getFormattedCooldownTime(lastCommandUse - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
                                    )
                            ),
                    250
            );
            return CommandResult.SUCCESS;
        }


        final String userData = this.gson.toJson(this.dsgvoModule.getUserData(userId));
        commandParameters.getUserTextChannel().sendFile(
                userData.getBytes(StandardCharsets.UTF_8),
                "Your-personal-data.json"
        ).queue();

        this.cooldownCache.put(userId, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + TimeUnit.DAYS.toSeconds(1));
        return CommandResult.SUCCESS;
    }
}
