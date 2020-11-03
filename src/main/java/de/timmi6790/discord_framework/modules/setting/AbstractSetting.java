package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import net.dv8tion.jda.api.utils.MarkdownUtil;

@Data
public abstract class AbstractSetting<T> {
    private final String internalName;
    private final String name;
    private final String defaultValue;
    private int databaseId;

    protected AbstractSetting(final String internalName, final String name, final String defaultValue) {
        this.name = name;
        this.internalName = internalName;
        this.defaultValue = defaultValue;
    }

    public abstract void handleCommand(CommandParameters commandParameters, String newValue);

    public abstract String toDatabaseValue(T value);

    public abstract T fromDatabaseValue(String value);

    protected void sendChangedValueMessage(final CommandParameters commandParameters, final String oldValue, final String newValue) {
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Changed Setting")
                        .setDescription(
                                "Changed value from %s to %s.",
                                MarkdownUtil.monospace(oldValue),
                                MarkdownUtil.monospace(newValue)
                        ),
                300
        );
    }
}
