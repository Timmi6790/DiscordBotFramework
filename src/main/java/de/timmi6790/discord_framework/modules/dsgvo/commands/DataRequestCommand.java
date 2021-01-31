package de.timmi6790.discord_framework.modules.dsgvo.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.CooldownCommandProperty;
import de.timmi6790.discord_framework.modules.dsgvo.DsgvoModule;
import de.timmi6790.discord_framework.modules.dsgvo.events.UserDataRequestEvent;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Data request command.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class DataRequestCommand extends AbstractCommand {
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();

    /**
     * The Dsgvo module.
     */
    private final DsgvoModule dsgvoModule;

    /**
     * Instantiates a new Data request command.
     */
    public DataRequestCommand() {
        super("giveMeMyData", "Info", "Get all my data!", "");

        this.addProperties(
                new CooldownCommandProperty(1, TimeUnit.DAYS)
        );

        this.dsgvoModule = this.getModuleManager().getModuleOrThrow(DsgvoModule.class);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final UserDataRequestEvent dataRequestEvent = new UserDataRequestEvent(
                commandParameters.getJda(),
                commandParameters.getUserDb()
        );
        this.getEventModule().executeEvent(dataRequestEvent);

        final String userData = this.getGson().toJson(dataRequestEvent.getDataMap());
        commandParameters.getUserTextChannel().sendFile(
                userData.getBytes(StandardCharsets.UTF_8),
                "Your-personal-data.json"
        ).queue();

        // Inform the user that his data is in his dms
        if (commandParameters.isGuildCommand()) {
            DiscordMessagesUtilities.sendMessageTimed(
                    commandParameters.getGuildTextChannel(),
                    new MultiEmbedBuilder()
                            .setTitle("Your Personal Data")
                            .setDescription("Check your private messages with the bot to access your personal data."),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }
}
