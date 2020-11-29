package de.timmi6790.discord_framework.modules.guild.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;

public class GuildSettingsCommand extends AbstractCommand {
    public GuildSettingsCommand() {
        super("guildSettings",
                "Info",
                "Settings",
                "[setting] [newValue]",
                "gst", "guildSetting");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        return null;
    }
}
