package de.timmi6790.discord_framework.module.modules.command.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import lombok.Data;

@Data
public class AllowGuildMessageProperty implements CommandProperty<Boolean> {
    private final boolean allowGuildMessage;

    protected void sendErrorMessage(final CommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Error")
                        .appendDescription("You can not execute this command in a guild.")
        );
    }

    @Override
    public Boolean getValue() {
        return this.allowGuildMessage;
    }

    @Override
    public boolean onCommandExecution(final Command command, final CommandParameters commandParameters) {
        if (this.allowGuildMessage || !commandParameters.isGuildCommand()) {
            return true;
        }

        this.sendErrorMessage(commandParameters);
        return false;
    }
}
