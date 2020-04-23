package de.timmi6790.statsbotdiscord.modules.core.commands.info;

import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;

public class InviteCommand extends AbstractCommand {
    public InviteCommand() {
        super("invite", "Info", "Invite the bot", "", "iv");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        return null;
    }
}
