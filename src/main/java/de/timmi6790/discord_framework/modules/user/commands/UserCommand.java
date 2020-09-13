package de.timmi6790.discord_framework.modules.user.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import de.timmi6790.discord_framework.modules.user.UserDb;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class UserCommand extends AbstractCommand<UserDbModule> {
    private static final String ERROR_TITLE = "Error";

    public UserCommand() {
        super("user", "Management", "User control command", "<discordUser> <perms|rank|achievement|setting|setPrimaryRank|ban|" +
                "unBan|info|invalidate> <add;remove;list|add;remove|add|add|rank|||||> <command;permNode|rank|>");

        this.addProperty(
                new MinArgCommandProperty(2)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // <discordUser> <achievement> <add> // TODO: Add more modes; remove
        // <discordUser> <setting> <add> // TODO: Add more modes; remove, change
        // <discordUser> <perms> <add|remove> <command|permNode>
        // <discordUser> <rank> <add|remove|list> <rank>
        // <discordUser> <setPrimaryRank> <rank>
        // <discordUser> <ban>
        // <discordUser> <unBan>
        // <discordUser> <info>
        // <discordUser> <invalidate>

        final User user = this.getDiscordUserThrow(commandParameters, 0);
        final UserDb userDb = this.getModule().getOrCreate(user.getIdLong());
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
            case SETTING:
                return this.settingCommand(commandParameters, userDb);
            case ACHIEVEMENT:
                return this.achievementCommand(commandParameters, userDb);
            case INVALIDATE:
                return this.invalidateCommand(commandParameters, userDb);
            default:
                return CommandResult.ERROR;
        }
    }

    private CommandResult invalidateCommand(final CommandParameters commandParameters, final UserDb userDb) {
        this.getModule().getModuleOrThrow(UserDbModule.class).getCache().invalidate(userDb.getDiscordId());

        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalidated Cache")
                        .setDescription(MarkdownUtil.monospace(String.valueOf(userDb.getDatabaseId())) + " was removed from the cache."),
                90
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult infoCommand(final CommandParameters commandParameters, final UserDb userDb) {
        final int commandSpamCache = this.getModule().getModuleOrThrow(CommandModule.class).getCommandSpamCache().get(userDb.getDiscordId()).get();
        final int activeEmotes = this.getModule().getModuleOrThrow(EmoteReactionModule.class).getActiveEmotesPerPlayer().getOrDefault(userDb.getDiscordId(), new AtomicInteger(0)).get();

        final String settings = "TODO";
                /*userDb.getSettingsMap()
                .entrySet()
                .stream()
                .map(setting -> setting.getKey().getInternalName() + ": " + setting.getKey().fromDatabaseValue(setting.getValue()))
                .collect(Collectors.joining("\n"));

                 */

        final String stats = userDb.getStatsMap()
                .entrySet()
                .stream()
                .map(setting -> setting.getKey().getInternalName() + ": " + setting.getValue())
                .collect(Collectors.joining("\n"));

        final RankModule rankModule = this.getModule().getModuleOrThrow(RankModule.class);
        final String primaryRank = rankModule.getRank(userDb.getPrimaryRank())
                .map(Rank::getName)
                .orElse("Unknown");
        final String subRanks = userDb.getRanks()
                .stream()
                .map(rankModule::getRank)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Rank::getName)
                .collect(Collectors.joining("; "));

        final String permissions = userDb.getPermissionIds()
                .stream()
                .map(this.getModule().getModuleOrThrow(PermissionsModule.class)::getPermissionFromId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining("\n"));

        final String allPermissions = userDb.getAllPermissionIds()
                .stream()
                .map(this.getModule().getModuleOrThrow(PermissionsModule.class)::getPermissionFromId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining("\n"));

        sendTimedMessage(commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("User Info")
                        .addField("Command Spam Cache", String.valueOf(commandSpamCache), true)
                        .addField("Active Emotes", String.valueOf(activeEmotes), true)
                        .addField("Shop Points", String.valueOf(userDb.getPoints()), false)
                        .addField("Ranks", primaryRank + "[" + subRanks + "]", true)
                        .addField("Settings", settings, false)
                        .addField("Stats", stats, false)
                        .addField("User Perms", permissions, false)
                        .addField("All Perms", allPermissions, false),
                90
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult unBanCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        if (!userDb.isBanned()) {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is not banned."),
                    90
            );
            return CommandResult.SUCCESS;
        }

        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Banned")
                        .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is now unBanned."),
                90
        );
        userDb.setBanned(false);

        return CommandResult.SUCCESS;
    }

    private CommandResult banCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        if (userDb.isBanned()) {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is already banned."),
                    90
            );
            return CommandResult.SUCCESS;
        }

        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
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
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription("The user already has this rank."),
                    90
            );

            return CommandResult.FAIL;
        }

        userDb.setPrimaryRank(rank);
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Set Primary Rank")
                        .setDescription("Set primary rank to " + MarkdownUtil.monospace(rank.getName()) + "."),
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
                sendTimedMessage(
                        commandParameters,
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription("The user already has this rank."),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.addRank(rank);
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Added Rank")
                            .setDescription("Added " + MarkdownUtil.monospace(rank.getName()) + " rank to the user."),
                    90
            );

        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!userDb.hasRank(rank)) {
                sendTimedMessage(
                        commandParameters,
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription("The user is not in possession of this rank."),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.removeRank(rank);
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Removed Rank")
                            .setDescription("Removed " + MarkdownUtil.monospace(rank.getName()) + " rank from the user."),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult permsCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = this.getFromEnumIgnoreCaseThrow(commandParameters, 2, AddRemoveArgs.values());
        final int permissionId = this.getPermissionIdThrow(commandParameters, 3);
        final String permissionNode = this.getModule().getModuleOrThrow(PermissionsModule.class).getPermissionFromId(permissionId)
                .orElseThrow(RuntimeException::new);

        if (AddRemoveArgs.ADD == mode) {
            if (userDb.hasPermission(permissionId)) {
                sendTimedMessage(
                        commandParameters,
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " does already possess the " + MarkdownUtil.monospace(permissionNode) + " permission."),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.addPermission(permissionId);
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Added Permission")
                            .setDescription(MarkdownUtil.monospace(permissionNode) + " added to " + MarkdownUtil.monospace(discordUser.getAsTag())),
                    90
            );

        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!userDb.hasPermission(permissionId)) {
                sendTimedMessage(
                        commandParameters,
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " does not possess the " + MarkdownUtil.monospace(permissionNode) + " permission."),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.removePermission(permissionId);
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Removed Permission")
                            .setDescription(MarkdownUtil.monospace(permissionNode) + " removed from " + MarkdownUtil.monospace(discordUser.getAsTag())),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult settingCommand(final CommandParameters commandParameters, final UserDb userDb) {
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Not implemented")
                        .setDescription("This is currently not implemented"),
                90
        );
        return CommandResult.SUCCESS;
    }

    private CommandResult achievementCommand(final CommandParameters commandParameters, final UserDb userDb) {
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Not implemented")
                        .setDescription("This is currently not implemented"),
                90
        );
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
        SETTING,
        ACHIEVEMENT,
        INVALIDATE
    }

    private enum AddRemoveArgs {
        ADD,
        REMOVE
    }
}
