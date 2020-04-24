package de.timmi6790.statsbotdiscord.modules.core.commands.info;

import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;

public class InviteCommand extends AbstractCommand {
    public InviteCommand() {
        super("invite", "Info", "Invite me.", "", "iv");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        this.sendTimedMessage(commandParameters,
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Invite Link")
                        .setDescription("[Click Me!](https://discordapp.com/api/oauth2/authorize?client_id=572110505964077056&permissions=67471424&scope=bot)"),
                90);
        return CommandResult.SUCCESS;
    }
}
