package de.timmi6790.statsbotdiscord.modules.core.commands.info;

import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;

public class AboutCommand extends AbstractCommand {
    public AboutCommand() {
        super("about", "Info", "About the bot", "");
        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        return null;
    }
}
