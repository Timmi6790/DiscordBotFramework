package de.timmi6790.statsbotdiscord.modules.core.commands.info;

import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;

public class AccountDeletionCommand extends AbstractCommand {
    private static final String[] RANDOM_CONFIRM_PHRASES = new String[]{};

    public AccountDeletionCommand() {
        super("deleteMyAccount", "Info", "Used wipe all your data.", "");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        return null;
    }
}
