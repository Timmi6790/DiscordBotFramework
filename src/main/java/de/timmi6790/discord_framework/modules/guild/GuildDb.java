package de.timmi6790.discord_framework.modules.guild;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(exclude = {"discord"})
@ToString(exclude = {"discord"})
public class GuildDb {
    private final int databaseId;
    private final long discordId;

    private final boolean banned;

    private final Set<String> commandAliasNames;
    private final Pattern commandAliasPattern;

    private final Map<String, AbstractSetting<?>> properties;

    private final JDA discord;

    private final LoadingCache<Long, Member> memberCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(key -> {
                final CompletableFuture<Member> futureValue = new CompletableFuture<>();
                this.getGuild().retrieveMemberById(key, false).queue(futureValue::complete);
                return futureValue.get(1, TimeUnit.MINUTES);
            });

    public GuildDb(final JDA discord,
                   final int databaseId,
                   final long discordId,
                   final boolean banned,
                   final Set<String> commandAliasNames,
                   final Map<String, AbstractSetting<?>> properties) {
        this.discord = discord;

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
        return this.discord.getGuildById(this.discordId);
    }

    public Member getMember(@NonNull final User user) {
        return this.getMember(user.getIdLong());
    }

    public Member getMember(final long userId) {
        return this.memberCache.get(userId);
    }

    public Optional<Pattern> getCommandAliasPattern() {
        return Optional.ofNullable(this.commandAliasPattern);
    }

    public boolean addCommandAlias(final String alias) {
        // Insert db
        return this.commandAliasNames.add(alias);
    }
}
