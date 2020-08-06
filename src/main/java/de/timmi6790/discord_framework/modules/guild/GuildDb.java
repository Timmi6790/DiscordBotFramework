package de.timmi6790.discord_framework.modules.guild;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class GuildDb {
    private final int databaseId;
    private final long discordId;

    private final boolean banned;

    private final Set<String> commandAliasNames;
    private final Pattern commandAliasPattern;

    private final Map<String, AbstractSetting> properties;
    
    public GuildDb(final int databaseId, final long discordId, final boolean banned, final Set<String> commandAliasNames, final Map<String, AbstractSetting> properties) {
        this.databaseId = databaseId;
        this.discordId = discordId;
        this.banned = banned;
        this.commandAliasNames = commandAliasNames;
        this.properties = properties;

        // TODO: Escape the alias names or limit the alias names
        if (commandAliasNames.isEmpty()) {
            this.commandAliasPattern = null;

        } else {
            final String aliasPattern = commandAliasNames
                    .stream()
                    .map(alias -> "(?:" + alias + ")")
                    .collect(Collectors.joining("|"));
            this.commandAliasPattern = Pattern.compile("^(?:" + aliasPattern + ")(.*)$)", Pattern.CASE_INSENSITIVE);
        }
    }

    public Guild getGuild() {
        return DiscordBot.getDiscord().getGuildById(this.discordId);
    }

    public Optional<Pattern> getCommandAliasPattern() {
        return Optional.ofNullable(this.commandAliasPattern);
    }

    public boolean addCommandAlias(final String alias) {
        // Insert db
        return this.commandAliasNames.add(alias);
    }
}
