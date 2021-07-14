package de.timmi6790.discord_framework.module.modules.core.commands.info;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import lombok.EqualsAndHashCode;

/**
 * Invite command.
 */
@EqualsAndHashCode(callSuper = true)
public class InviteCommand extends Command {
    /**
     * The Invite url.
     */
    private final String inviteUrl;

    /**
     * Instantiates a new Invite command.
     *
     * @param inviteUrl the invite url
     */
    public InviteCommand(final String inviteUrl,
                         final CommandModule commandModule) {
        super("invite", commandModule);

        this.inviteUrl = inviteUrl;

        this.addProperties(
                new CategoryProperty("Info"),
                new DescriptionProperty("Invite me"),
                new AliasNamesProperty("iv")
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invite Link")
                        .setDescription("[Click Me!](" + this.inviteUrl + ")")
        );
        return BaseCommandResult.SUCCESSFUL;
    }
}
