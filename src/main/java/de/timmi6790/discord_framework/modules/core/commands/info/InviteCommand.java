package de.timmi6790.discord_framework.modules.core.commands.info;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;

public class InviteCommand extends AbstractCommand {
    private final String inviteUrl;

    public InviteCommand(final String inviteUrl) {
        super("invite", "Info", "Invite me.", "", "iv");

        this.inviteUrl = inviteUrl;

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        this.sendTimedMessage(commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invite Link")
                        .setDescription("[Click Me!](" + this.inviteUrl + ")"),
                90);
        return CommandResult.SUCCESS;
    }
}
