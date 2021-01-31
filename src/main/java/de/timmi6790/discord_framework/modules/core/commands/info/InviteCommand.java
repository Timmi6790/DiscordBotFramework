package de.timmi6790.discord_framework.modules.core.commands.info;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import lombok.EqualsAndHashCode;

/**
 * Invite command.
 */
@EqualsAndHashCode(callSuper = true)
public class InviteCommand extends AbstractCommand {
    /**
     * The Invite url.
     */
    private final String inviteUrl;

    /**
     * Instantiates a new Invite command.
     *
     * @param inviteUrl the invite url
     */
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
