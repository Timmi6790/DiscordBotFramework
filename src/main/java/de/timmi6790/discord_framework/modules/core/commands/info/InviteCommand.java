package de.timmi6790.discord_framework.modules.core.commands.info;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.core.CoreModule;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class InviteCommand extends AbstractCommand<CoreModule> {
    private final String inviteUrl;

    public InviteCommand(final String inviteUrl) {
        super("invite", "Info", "Invite me.", "", "iv");

        this.inviteUrl = inviteUrl;
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        sendTimedMessage(commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Invite Link")
                        .setDescription("[Click Me!](" + this.inviteUrl + ")"),
                90);
        return CommandResult.SUCCESS;
    }
}
