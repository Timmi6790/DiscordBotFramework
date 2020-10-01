package de.timmi6790.discord_framework.modules.setting.commands;

import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;

import java.util.HashMap;
import java.util.Map;

public class SettingsCommand extends AbstractCommand {
    public SettingsCommand() {
        super("settings", "Info", "Settings", "[setting] [value]", "", "st");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // All current settings
        if (commandParameters.getArgs().length == 0) {
            final MultiEmbedBuilder embedBuilder = getEmbedBuilder(commandParameters)
                    .setTitle("Settings");

            // TODO: Fix me after new settings module
            final Map<AbstractSetting<?>, String> settingsMap = new HashMap<>(); // commandParameters.getUserDb().getSettingsMap();
            if (settingsMap.isEmpty()) {
                embedBuilder.setDescription("You can unlock settings by using the bot.");
            } else {
                settingsMap.forEach((settings, value) -> embedBuilder.addField(settings.getName(), value, false));
            }

            sendTimedMessage(
                    commandParameters,
                    embedBuilder,
                    90
            );
            return CommandResult.SUCCESS;
        }

        this.checkArgLength(commandParameters, 2);

        return CommandResult.SUCCESS;
    }
}
