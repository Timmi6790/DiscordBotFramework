package de.timmi6790.discord_framework.module.modules.setting;

import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Data
public abstract class AbstractSetting<T> {
    private final String statName;
    private final String description;
    private final T defaultValue;
    private final String[] aliasNames;
    private String internalName;
    private int databaseId;
    private int permissionId;

    protected AbstractSetting(@NonNull final String statName,
                              @NonNull final String description,
                              @NonNull final T defaultValue,
                              @NonNull final String... aliasNames) {
        this.statName = statName;
        this.description = description;
        this.defaultValue = defaultValue;
        this.aliasNames = aliasNames;
    }

    public abstract String toDatabaseValue(T value);

    public abstract T fromDatabaseValue(String value);

    protected abstract Optional<T> parseNewValue(CommandParameters commandParameters, String userInput);

    protected abstract List<T> possibleValues(CommandParameters commandParameters, String userInput);

    public String getDatabaseDefaultValue() {
        return this.toDatabaseValue(this.getDefaultValue());
    }

    public void handleCommand(final CommandParameters commandParameters, final String userInput) {
        final Optional<T> parsedValue = this.parseNewValue(commandParameters, userInput);
        if (parsedValue.isPresent()) {
            final T oldValue = commandParameters.getUserDb().getSettingOrDefault(this, this.getDefaultValue());
            this.changeValue(
                    commandParameters.getUserDb(),
                    parsedValue.get()
            );
            this.sendChangedValueMessage(
                    commandParameters,
                    oldValue,
                    parsedValue.get()
            );
            return;
        }

        this.sendInvalidInputMessage(commandParameters, userInput, this.possibleValues(commandParameters, userInput));
    }

    protected void changeValue(@NonNull final UserDb userDb,
                               @NonNull final T newValue) {
        userDb.setSetting(this, newValue);
    }

    protected void sendInvalidInputMessage(final CommandParameters commandParameters,
                                           final String userInput,
                                           final Iterable<T> possibleValues) {
        final StringJoiner possibleValueFormatted = new StringJoiner("\n");
        for (final T possibleValue : possibleValues) {
            possibleValueFormatted.add(MarkdownUtil.monospace(String.valueOf(possibleValue)));
        }

        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                commandParameters.getEmbedBuilder()
                        .setTitle("Can't change setting")
                        .setDescription(
                                "%s it not a valid value.\nPlease use one of the following values:\n%s",
                                MarkdownUtil.monospace(userInput),
                                possibleValueFormatted.toString()
                        ),
                300
        );
    }

    protected void sendChangedValueMessage(final CommandParameters commandParameters,
                                           final T oldValue,
                                           final T newValue) {
        final String oldValueString = String.valueOf(oldValue);
        final String oldValueFormatted = oldValueString.isEmpty() ? EmbedBuilder.ZERO_WIDTH_SPACE : oldValueString;
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                commandParameters.getEmbedBuilder()
                        .setTitle("Changed Setting")
                        .setDescription(
                                "Changed value from %s to %s.",
                                MarkdownUtil.monospace(oldValueFormatted),
                                MarkdownUtil.monospace(String.valueOf(newValue))
                        ),
                300
        );
    }
}
