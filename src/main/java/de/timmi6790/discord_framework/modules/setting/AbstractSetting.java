package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.datatypes.builders.MapBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.database.DatabaseGetId;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.*;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
public abstract class AbstractSetting<T> extends DatabaseGetId {
    private static final String SETTING_NAME = "settingName";

    private static final String GET_SETTING_ID = "SELECT id FROM setting WHERE setting_name = :settingName LIMIT 1;";
    private static final String INSERT_SETTING = "INSERT INTO setting(setting_name) VALUES(:settingName);";

    private final int databaseId;
    private final String internalName;
    private final String name;
    private final String defaultValue;

    public AbstractSetting(final String internalName, final String name, final String defaultValue) {
        super(GET_SETTING_ID, INSERT_SETTING);

        this.name = name;
        this.internalName = internalName;
        this.defaultValue = defaultValue;
        this.databaseId = this.retrieveDatabaseId();
    }

    @Override
    protected @NonNull Map<String, Object> getGetIdParameters() {
        return MapBuilder.<String, Object>ofHashMap()
                .put(SETTING_NAME, this.getInternalName())
                .build();
    }

    @Override
    protected @NonNull Map<String, Object> getInsertIdParameters() {
        return MapBuilder.<String, Object>ofHashMap()
                .put(SETTING_NAME, this.getInternalName())
                .build();
    }

    public abstract void handleCommand(CommandParameters commandParameters, String newValue);

    public abstract String toDatabaseValue(T value);

    public abstract T fromDatabaseValue(String value);

    protected void sendChangedValueMessage(final CommandParameters commandParameters, final String oldValue, final String newValue) {
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Changed Setting")
                        .setDescription("Changed value from %s to %s.", MarkdownUtil.monospace(oldValue), MarkdownUtil.monospace(newValue)),
                300
        );
    }
}
