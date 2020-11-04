package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public abstract class AbstractSetting<T> {
    private final String internalName;
    private final String name;
    private final String description;
    private final String defaultDatabaseValue;
    private int databaseId;
    private int permissionId;

    protected AbstractSetting(final String internalName, final String name, final String description, final String defaultDatabaseValue) {
        this.name = name;
        this.internalName = internalName;
        this.description = description;
        this.defaultDatabaseValue = defaultDatabaseValue;
    }

    public abstract String toDatabaseValue(T value);

    public abstract T fromDatabaseValue(String value);

    protected abstract Optional<T> parseNewValue(CommandParameters commandParameters, final String userInput);

    protected abstract List<T> possibleValues(CommandParameters commandParameters, String userInput);

    public void handleCommand(final CommandParameters commandParameters, final String userInput) {
        final Optional<T> parsedValue = this.parseNewValue(commandParameters, userInput);
        if (parsedValue.isPresent()) {
            this.changeValue(
                    commandParameters,
                    parsedValue.get(),
                    true
            );
            return;
        }

        this.sendInvalidInputMessage(commandParameters, userInput, this.possibleValues(commandParameters, userInput));
    }

    protected void changeValue(final CommandParameters commandParameters,
                               final T newValue,
                               final boolean sendMessage) {
        final T oldValue = commandParameters.getUserDb().getSetting(this).orElse(this.fromDatabaseValue(this.getDefaultDatabaseValue()));
        commandParameters.getUserDb().setSetting(this, newValue);
        if (sendMessage) {
            this.sendChangedValueMessage(
                    commandParameters,
                    oldValue,
                    newValue
            );
        }
    }

    protected void sendInvalidInputMessage(final CommandParameters commandParameters,
                                           final String userInput,
                                           final List<T> possibleValues) {
        final List<String> possibleValueFormatted = new ArrayList<>();
        for (final T possibleValue : possibleValues) {
            possibleValueFormatted.add(MarkdownUtil.monospace(String.valueOf(possibleValue)));
        }

        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Can't change setting")
                        .setDescription(
                                "%s it not a valid value.\nPlease use one of the following values:\n%s",
                                MarkdownUtil.monospace(userInput),
                                String.join("\n", possibleValueFormatted)
                        ),
                300
        );
    }

    protected void sendChangedValueMessage(final CommandParameters commandParameters,
                                           final T oldValue,
                                           final T newValue) {
        final String oldValueString = String.valueOf(oldValue);
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Changed Setting")
                        .setDescription(
                                "Changed value from %s to %s.",
                                MarkdownUtil.monospace(oldValueString.isEmpty() ? EmbedBuilder.ZERO_WIDTH_SPACE : oldValueString),
                                MarkdownUtil.monospace(String.valueOf(newValue))
                        ),
                300
        );
    }
}
