package de.timmi6790.discord_framework.module.modules.guild;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Data
public class GuildDb {
    private final long discordId;
    private final boolean banned;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final ShardManager discord;

    private final LoadingCache<Long, Member> memberCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(key -> {
                final CompletableFuture<Member> futureValue = new CompletableFuture<>();
                this.getGuild().retrieveMemberById(key).queue(futureValue::complete);
                return futureValue.get(1, TimeUnit.MINUTES);
            });

    public GuildDb(final ShardManager discord,
                   final long discordId,
                   final boolean banned) {
        this.discord = discord;

        this.discordId = discordId;
        this.banned = banned;
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

    public boolean isPrivateMessage() {
        return this.discordId == GuildDbModule.getPrivateMessageGuildId();
    }
}
