package de.timmi6790.discord_framework.module.modules.dsgvo.commands;

import de.timmi6790.discord_framework.module.modules.dsgvo.DsgvoModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.controll.CooldownProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
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
public class DataRequestCommand extends SlashCommand {
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
                              final SlashCommandModule module) {
        super(module, "giveMeMyData", "Get all my data!");

        this.addProperties(
                new CategoryProperty("Info"),
                new CooldownProperty(1, TimeUnit.DAYS)
        );

        this.dsgvoModule = dsgvoModule;
    }

    @Override
    protected CommandResult onCommand(final SlashCommandParameters commandParameters) {
        final String userData = this.dsgvoModule.getUserData(commandParameters.getUserDb());
        commandParameters.getUserTextChannel().sendFile(
                userData.getBytes(StandardCharsets.UTF_8),
                "Your-personal-data.json"
        ).queue();

        // Inform the user that his data is in his dms
        if (commandParameters.isGuildCommand()) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Your Personal Data")
                            .setDescription("Check your private messages with the bot to access your personal data.")
            );
        }

        return BaseCommandResult.SUCCESSFUL;
    }
}
