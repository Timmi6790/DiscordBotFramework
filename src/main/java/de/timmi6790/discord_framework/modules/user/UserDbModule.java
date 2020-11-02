package de.timmi6790.discord_framework.modules.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.commands.UserCommand;
import de.timmi6790.discord_framework.modules.user.repository.UserDbRepository;
import de.timmi6790.discord_framework.modules.user.repository.UserDbRepositoryMysql;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
@Getter
public class UserDbModule extends AbstractModule {
    private final Cache<Long, UserDb> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private UserDbRepository userDbRepository;

    public UserDbModule() {
        super("UserDb");

        this.addDependenciesAndLoadAfter(
                DatabaseModule.class,
                PermissionsModule.class,
                CommandModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.userDbRepository = new UserDbRepositoryMysql(this);

        this.getModuleOrThrow(CommandModule.class)
                .registerCommands(
                        this,
                        new UserCommand()
                );
    }

    protected UserDb create(final long discordId) {
        // Make sure that the user is not present
        final Optional<UserDb> userDbOpt = this.get(discordId);
        if (userDbOpt.isPresent()) {
            return userDbOpt.get();
        }

        final UserDb userDb = this.getUserDbRepository().create(discordId);
        this.getCache().put(discordId, userDb);
        return userDb;
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
        this.getUserDbRepository().delete(userDb);
        this.getCache().invalidate(userDb.getDiscordId());
    }
}
