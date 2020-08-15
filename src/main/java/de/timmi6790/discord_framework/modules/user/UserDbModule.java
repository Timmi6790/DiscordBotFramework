package de.timmi6790.discord_framework.modules.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.user.commands.UserCommand;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class UserDbModule extends AbstractModule {
    private static final String GET_PLAYER = "SELECT player.id, player.discordId, player.shop_points shopPoints, player.banned, player.primary_rank primaryRank, " +
            "GROUP_CONCAT(DISTINCT p_rank.rank_id) ranks, " +
            "GROUP_CONCAT(DISTINCT permission.id) perms, " +
            "GROUP_CONCAT(DISTINCT CONCAT_WS(',', p_setting.setting_id, p_setting.setting) SEPARATOR ';') settings, " +
            "GROUP_CONCAT(DISTINCT CONCAT_WS(',', p_stat.stat_id, p_stat.value) SEPARATOR ';') stats, " +
            "GROUP_CONCAT(DISTINCT p_ach.achievement_id) achievements " +
            "FROM player  " +
            "LEFT JOIN player_rank p_rank ON p_rank.player_id = player.id  " +
            "LEFT JOIN player_permission p_perm ON p_perm.player_id = player.id  " +
            "LEFT JOIN permission ON permission.default_permission = 1 OR permission.id = p_perm.permission_id  " +
            "LEFT JOIN player_setting p_setting ON p_setting.player_id = player.id  " +
            "LEFT JOIN player_stat p_stat ON p_stat.player_id = player.id " +
            "LEFT JOIN player_achievement p_ach ON p_ach.player_id = player.id " +
            "WHERE player.discordId = :discordId LIMIT 1;";
    private static final String INSERT_PLAYER = "INSERT INTO player(discordId) VALUES (:discordId);";
    private static final String REMOVE_PLAYER = "DELETE FROM player WHERE id = :dbId LIMIT 1;";
    @Getter
    private final Cache<Long, UserDb> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

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
        this.getModuleOrThrow(DatabaseModule.class).getJdbi()
                .registerRowMapper(UserDb.class, new UserDbMapper());

        this.getModuleOrThrow(CommandModule.class)
                .registerCommands(
                        this,
                        new UserCommand()
                );
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    private UserDb create(final long discordId) {
        // Make sure that the user is not present
        return this.get(discordId).orElseGet(() -> {
            this.getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                    handle.createUpdate(INSERT_PLAYER)
                            .bind("discordId", discordId)
                            .execute()
            );

            // Should never throw
            return this.get(discordId).orElseThrow(RuntimeException::new);
        });
    }

    public Optional<UserDb> get(final long discordId) {
        final UserDb userDbCache = this.cache.getIfPresent(discordId);
        if (userDbCache != null) {
            return Optional.of(userDbCache);
        }

        final Optional<UserDb> userDbOpt = this.getModuleOrThrow(DatabaseModule.class).getJdbi().withHandle(handle ->
                handle.createQuery(GET_PLAYER)
                        .bind("discordId", discordId)
                        .mapTo(UserDb.class)
                        .findFirst()
        );
        userDbOpt.ifPresent(userDb -> this.cache.put(discordId, userDb));

        return userDbOpt;
    }

    public UserDb getOrCreate(final long discordId) {
        return this.get(discordId).orElseGet(() -> this.create(discordId));
    }

    public void delete(final UserDb userDb) {
        this.delete(userDb.getDiscordId());
    }

    public void delete(final long discordId) {
        this.getModuleOrThrow(DatabaseModule.class).getJdbi().useHandle(handle ->
                handle.createUpdate(REMOVE_PLAYER)
                        .bind("dbId", discordId)
                        .execute()
        );
        this.cache.invalidate(discordId);
    }
}
