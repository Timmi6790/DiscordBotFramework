package de.timmi6790.discord_framework.module.modules.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.Striped;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.achievement.AchievementModule;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.stat.StatModule;
import de.timmi6790.discord_framework.module.modules.user.commands.SettingsCommand;
import de.timmi6790.discord_framework.module.modules.user.commands.UserCommand;
import de.timmi6790.discord_framework.module.modules.user.listeners.DsgvoListener;
import de.timmi6790.discord_framework.module.modules.user.repository.UserDbRepository;
import de.timmi6790.discord_framework.module.modules.user.repository.postgres.UserDbPostgresRepository;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@EqualsAndHashCode(callSuper = true)
@Getter
public class UserDbModule extends AbstractModule {
    private final LoadingCache<Long, User> discordUserCache = Caffeine.newBuilder()
            .recordStats()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build(key -> {
                final CompletableFuture<User> futureValue = new CompletableFuture<>();
                this.discord.retrieveUserById(key).queue(futureValue::complete);
                return futureValue.get(1, TimeUnit.MINUTES);
            });

    private final Striped<Lock> userCreateLock = Striped.lock(64);
    private final Cache<Long, UserDb> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private UserDbRepository userDbRepository;
    private ShardManager discord;

    public UserDbModule() {
        super("UserDb");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                PermissionsModule.class,
                CommandModule.class,
                EventModule.class,
                RankModule.class
        );

        this.addLoadAfterDependencies(
                SettingModule.class,
                MetricModule.class
        );

        this.addDependencies(
                CommandModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.discord = super.getDiscord();
        final EventModule eventModule = this.getModuleOrThrow(EventModule.class);
        this.userDbRepository = new UserDbPostgresRepository(
                this,
                this.getModuleOrThrow(DatabaseModule.class),
                eventModule,
                this.getModuleOrThrow(RankModule.class),
                this.getModule(AchievementModule.class).orElse(null),
                this.getModule(SettingModule.class).orElse(null),
                this.getModule(StatModule.class).orElse(null)
        );

        final CommandModule commandModule = this.getModuleOrThrow(CommandModule.class);
        commandModule
                .registerCommands(
                        this,
                        new UserCommand(
                                this,
                                this.getModuleOrThrow(PermissionsModule.class),
                                this.getModuleOrThrow(RankModule.class),
                                this.getModule(SettingModule.class).orElse(null),
                                commandModule,
                                eventModule
                        )
                );

        this.getModuleOrThrow(EventModule.class)
                .addEventListeners(
                        new DsgvoListener(this)
                );

        this.getModule(SettingModule.class).ifPresent(settingModule ->
                commandModule
                        .registerCommands(
                                this,
                                new SettingsCommand(
                                        settingModule,
                                        commandModule,
                                        eventModule
                                )
                        )
        );

        // Register metrics
        this.getModule(MetricModule.class).ifPresent(metric ->
                CaffeineCacheMetrics.monitor(
                        metric.getMeterRegistry(),
                        this.cache,
                        "userDB_user"
                )
        );

        return true;
    }

    protected UserDb create(final long discordId) {
        // Lock the current discord id to prevent multiple creates
        final Lock lock = this.userCreateLock.get(discordId);
        lock.lock();
        try {
            // Make sure that the user is not present
            final Optional<UserDb> userDbOpt = this.get(discordId);
            if (userDbOpt.isPresent()) {
                return userDbOpt.get();
            }

            final UserDb userDb = this.getUserDbRepository().create(discordId);
            this.getCache().put(discordId, userDb);
            return userDb;
        } finally {
            lock.unlock();
        }
    }

    public Optional<UserDb> get(final long discordId) {
        final UserDb userDbCache = this.getCache().getIfPresent(discordId);
        if (userDbCache != null) {
            return Optional.of(userDbCache);
        }

        final Optional<UserDb> userDbOpt = this.getUserDbRepository().get(discordId);
        userDbOpt.ifPresent(userDb -> this.getCache().put(discordId, userDb));

        return userDbOpt;
    }

    public UserDb getOrCreate(final long discordId) {
        return this.get(discordId).orElseGet(() -> this.create(discordId));
    }

    public void delete(final long discordId) {
        this.get(discordId).ifPresent(this::delete);
    }

    public void delete(@NonNull final UserDb userDb) {
        this.getUserDbRepository().delete(userDb.getDiscordId());
        this.getCache().invalidate(userDb.getDiscordId());
    }
}
