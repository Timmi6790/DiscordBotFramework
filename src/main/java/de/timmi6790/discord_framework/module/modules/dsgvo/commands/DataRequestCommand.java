package de.timmi6790.discord_framework.module.modules.dsgvo.commands;

import de.timmi6790.discord_framework.module.modules.command_old.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command_old.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command_old.CommandResult;
import de.timmi6790.discord_framework.module.modules.command_old.property.properties.CooldownCommandProperty;
import de.timmi6790.discord_framework.module.modules.dsgvo.DsgvoModule;
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
    /**
     * The Dsgvo module.
     */
    private final DsgvoModule dsgvoModule;

    /**
     * Instantiates a new Data request command.
     */
    public DataRequestCommand(final DsgvoModule dsgvoModule) {
        super("giveMeMyData", "Info", "Get all my data!", "");

        this.addProperties(
                new CooldownCommandProperty(1, TimeUnit.DAYS)
        );

        this.dsgvoModule = dsgvoModule;
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String userData = this.dsgvoModule.getUserData(commandParameters.getUserDb());
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
