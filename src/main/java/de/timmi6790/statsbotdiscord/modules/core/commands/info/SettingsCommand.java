package de.timmi6790.statsbotdiscord.modules.core.commands.info;

import de.timmi6790.statsbotdiscord.datatypes.StatEmbedBuilder;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.setting.AbstractSetting;

import java.util.Map;

public class SettingsCommand extends AbstractCommand {
    public SettingsCommand() {
        super("settings", "Info", "Settings", "[setting] [value]", "", "st");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // All current settings
        if (commandParameters.getArgs().length == 0) {
            final StatEmbedBuilder embedBuilder = this.getEmbedBuilder(commandParameters)
                    .setTitle("Settings");

            final Map<AbstractSetting<?>, String> settingsMap = commandParameters.getUserDb().getSettingsMap();
            if (settingsMap.isEmpty()) {
                embedBuilder.setDescription("You can unlock settings by using the bot.");
            } else {
                settingsMap.forEach((settings, value) -> embedBuilder.addField(settings.getName(), value, false));
            }

            this.sendTimedMessage(
                    commandParameters,
                    embedBuilder,
                    90
            );
            return CommandResult.SUCCESS;
        }
        return CommandResult.SUCCESS;
    }
}
