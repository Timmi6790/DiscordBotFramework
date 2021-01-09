package de.timmi6790.discord_framework.modules.user.commands;

import de.timmi6790.discord_framework.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import io.github.bucket4j.Bucket;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

@EqualsAndHashCode(callSuper = true)
public class UserCommand extends AbstractCommand {
    private static final String ERROR_TITLE = "Error";

    @Getter(lazy = true)
    private final UserDbModule userDbModule = this.getModuleManager().getModuleOrThrow(UserDbModule.class);
    @Getter(lazy = true)
    private final EmoteReactionModule emoteReactionModule = this.getModuleManager().getModuleOrThrow(EmoteReactionModule.class);

    public UserCommand() {
        super("user",
                "Management",
                "User control command",
                "<discordUser> <perms|rank|setPrimaryRank|ban|unBan|info|invalidate> " +
                        "<add;remove;list|add;remove|rank|||||> <command;permNode|rank|>");

        this.addProperty(
                new MinArgCommandProperty(2)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // <discordUser> <perms> <add|remove> <command|permNode>
        // <discordUser> <rank> <add|remove|list> <rank>
        // <discordUser> <setPrimaryRank> <rank>
        // <discordUser> <ban>
        // <discordUser> <unBan>
        // <discordUser> <info>
        // <discordUser> <invalidate>

        final User user = this.getDiscordUserThrow(commandParameters, 0);
        final UserDb userDb = this.getUserDbModule().getOrCreate(user.getIdLong());
        final ValidArgs1 arg1 = this.getFromEnumIgnoreCaseThrow(commandParameters, 1, ValidArgs1.values());

        switch (arg1) {
            case INFO:
                return this.infoCommand(commandParameters, userDb);
            case UN_BAN:
                return this.unBanCommand(commandParameters, userDb, user);
            case BAN:
                return this.banCommand(commandParameters, userDb, user);
            case SET_PRIMARY_RANK:
                return this.setPrimaryRankCommand(commandParameters, userDb);
            case RANK:
                return this.rankCommand(commandParameters, userDb);
            case PERMS:
                return this.permsCommand(commandParameters, userDb, user);
            case INVALIDATE:
                return this.invalidateCommand(commandParameters, userDb);
            default:
                return CommandResult.ERROR;
        }
    }

    private CommandResult invalidateCommand(final CommandParameters commandParameters, final UserDb userDb) {
        this.getUserDbModule().getCache().invalidate(userDb.getDiscordId());

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalidated Cache")
                        .setDescription(MarkdownUtil.monospace(String.valueOf(userDb.getDatabaseId())) + " was removed from the cache."),
                90
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult infoCommand(final CommandParameters commandParameters, final UserDb userDb) {
        final Bucket commandRatelimit = this.getCommandModule().resolveRateBucket(userDb.getUser().getIdLong());
        final int activeEmotes = this.getEmoteReactionModule().getActiveEmotesPerPlayer().getOrDefault(userDb.getDiscordId(), new AtomicInteger(0)).get();

        final StringJoiner settings = new StringJoiner("\n");
        for (final Map.Entry<AbstractSetting<?>, String> entry : userDb.getSettings().entrySet()) {
            settings.add(entry.getKey().getInternalName() + ": " + entry.getValue());
        }

        final StringJoiner stats = new StringJoiner("\n");
        for (final Map.Entry<AbstractStat, Integer> entry : userDb.getStatsMap().entrySet()) {
            stats.add(entry.getKey().getInternalName() + ": " + entry.getValue());
        }

        final StringJoiner achievements = new StringJoiner("\n");
        for (final AbstractAchievement achievement : userDb.getAchievements()) {
            achievements.add(achievement.getAchievementName());
        }

        final String primaryRank = this.getRankModule().getRank(userDb.getPrimaryRankId())
                .map(Rank::getRankName)
                .orElse("Unknown");

        final StringJoiner subRanks = new StringJoiner("; ");
        for (final int rankId : userDb.getRankIds()) {
            this.getRankModule().getRank(rankId)
                    .map(Rank::getRankName)
                    .ifPresent(subRanks::add);
        }

        final StringJoiner permissions = new StringJoiner("\n");
        for (final int permissionId : userDb.getPermissionIds()) {
            this.getPermissionsModule().getPermissionFromId(permissionId).ifPresent(permissions::add);
        }

        final StringJoiner allPermissions = new StringJoiner("\n");
        for (final int permissionId : userDb.getAllPermissionIds()) {
            this.getPermissionsModule().getPermissionFromId(permissionId).ifPresent(allPermissions::add);
        }

        this.sendTimedMessage(commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("User Info")
                        .addField("Command Spam Cache", String.valueOf(commandRatelimit.getAvailableTokens()), true)
                        .addField("Active Emotes", String.valueOf(activeEmotes), true)
                        .addField("Ranks", primaryRank + "[" + subRanks + "]", true)
                        .addField("Achievements", achievements.toString(), false)
                        .addField("Settings", settings.toString(), false)
                        .addField("Stats", stats.toString(), false)
                        .addField("User Perms", permissions.toString(), false)
                        .addField("All Perms", allPermissions.toString(), false),
                90
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult unBanCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        if (!userDb.isBanned()) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is not banned."),
                    90
            );
            return CommandResult.SUCCESS;
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Banned")
                        .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is now unBanned."),
                90
        );
        userDb.setBanned(false);

        return CommandResult.SUCCESS;
    }

    private CommandResult banCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        if (userDb.isBanned()) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is already banned."),
                    90
            );
            return CommandResult.SUCCESS;
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Banned")
                        .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is now banned."),
                90
        );
        userDb.setBanned(true);

        return CommandResult.SUCCESS;
    }

    private CommandResult setPrimaryRankCommand(final CommandParameters commandParameters, final UserDb userDb) {
        this.checkArgLength(commandParameters, 3);

        final Rank rank = this.getRankThrow(commandParameters, 2);
        if (userDb.hasPrimaryRank(rank)) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription("The user already has this rank."),
                    90
            );

            return CommandResult.FAIL;
        }

        userDb.setPrimaryRank(rank);
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Set Primary Rank")
                        .setDescription(
                                "Set primary rank to %s.",
                                MarkdownUtil.monospace(rank.getRankName())
                        ),
                90
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult rankCommand(final CommandParameters commandParameters, final UserDb userDb) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = this.getFromEnumIgnoreCaseThrow(commandParameters, 2, AddRemoveArgs.values());
        final Rank rank = this.getRankThrow(commandParameters, 3);

        if (AddRemoveArgs.ADD == mode) {
            if (userDb.hasRank(rank)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription("The user already has this rank."),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.addRank(rank);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Added Rank")
                            .setDescription(
                                    "Added %s rank to the user.",
                                    MarkdownUtil.monospace(rank.getRankName())
                            ),
                    90
            );

        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!userDb.hasRank(rank)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription("The user is not in possession of this rank."),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.removeRank(rank);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Removed Rank")
                            .setDescription(
                                    "Removed %s rank from the user.",
                                    MarkdownUtil.monospace(rank.getRankName())
                            ),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult permsCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = this.getFromEnumIgnoreCaseThrow(commandParameters, 2, AddRemoveArgs.values());
        final int permissionId = this.getPermissionIdThrow(commandParameters, 3);
        final String permissionNode = this.getPermissionsModule().getPermissionFromId(permissionId)
                .orElseThrow(RuntimeException::new);

        if (AddRemoveArgs.ADD == mode) {
            if (userDb.hasPermission(permissionId)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s does already possess the %s permission.",
                                        MarkdownUtil.monospace(discordUser.getAsTag()),
                                        MarkdownUtil.monospace(permissionNode)
                                ),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.addPermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Added Permission")
                            .setDescription(
                                    "%s added to %s.",
                                    MarkdownUtil.monospace(permissionNode),
                                    MarkdownUtil.monospace(discordUser.getAsTag())
                            ),
                    90
            );

        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!userDb.hasPermission(permissionId)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s does not possess the %s permission.",
                                        MarkdownUtil.monospace(discordUser.getAsTag()),
                                        MarkdownUtil.monospace(permissionNode)
                                ),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.removePermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Removed Permission")
                            .setDescription(
                                    "%s removed from %s.",
                                    MarkdownUtil.monospace(permissionNode),
                                    MarkdownUtil.monospace(discordUser.getAsTag())
                            ),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    // Utilities
    private enum ValidArgs1 {
        INFO,
        UN_BAN,
        BAN,
        SET_PRIMARY_RANK,
        RANK,
        PERMS,
        INVALIDATE
    }

    private enum AddRemoveArgs {
        ADD,
        REMOVE
    }
}
