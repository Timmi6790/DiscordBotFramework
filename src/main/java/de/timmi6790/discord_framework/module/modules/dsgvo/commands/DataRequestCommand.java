package de.timmi6790.discord_framework.module.modules.dsgvo.commands;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.controll.CooldownProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import de.timmi6790.discord_framework.module.modules.dsgvo.DsgvoModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Data request command.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class DataRequestCommand extends Command {
    /**
     * The Dsgvo module.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final DsgvoModule dsgvoModule;

    /**
     * Instantiates a new Data request command.
     */
    public DataRequestCommand(final DsgvoModule dsgvoModule,
                              final CommandModule commandModule,
                              final EventModule eventModule) {
        super("giveMeMyData", commandModule, eventModule);

        this.addProperties(
                new CategoryProperty("Info"),
                new DescriptionProperty("Get all my data!"),
                new CooldownProperty(1, TimeUnit.DAYS)
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

        return BaseCommandResult.SUCCESSFUL;
    }
}
