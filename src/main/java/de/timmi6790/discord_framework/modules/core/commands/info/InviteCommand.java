package de.timmi6790.discord_framework.modules.core.commands.info;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class InviteCommand extends AbstractCommand {
    private final String inviteUrl;

    public InviteCommand(final String inviteUrl) {
        super("invite", "Info", "Invite me.", "", "iv");

        this.inviteUrl = inviteUrl;
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        this.sendTimedMessage(
                commandParameters,
                "Invite Link",
                "[Click Me!](" + this.inviteUrl + ")"
        );
        return CommandResult.SUCCESS;
    }
}
