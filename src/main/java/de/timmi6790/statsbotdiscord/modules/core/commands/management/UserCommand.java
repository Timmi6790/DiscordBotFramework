package de.timmi6790.statsbotdiscord.modules.core.commands.management;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.exceptions.CommandReturnException;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import de.timmi6790.statsbotdiscord.modules.rank.Rank;
import de.timmi6790.statsbotdiscord.modules.rank.RankManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class UserCommand extends AbstractCommand {
    private static final List<String> VALID_1_ARGS = new ArrayList<>(Arrays.asList("info", "unBan", "ban", "setPrimaryRank", "rank", "perms", "setting", "achievement"));
    private static final List<String> PERMS_2_ARGS = new ArrayList<>(Arrays.asList("add", "remove"));
    private static final List<String> RANK_2_ARGS = new ArrayList<>(Arrays.asList("add", "remove"));

    public UserCommand() {
        super("user", "Management", "User control command", "<>");

        this.setMinArgs(2);
        this.setPermission("core.management.user_control");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // <discordUser> <achievement> <add> // TODO: Add more modes; remove
        // <discordUser> <setting> <add> // TODO: Add more modes; remove, change
        // <discordUser> <perms> <add|remove> <command|permNode>
        // <discordUser> <rank> <add|remove|list> <rank|rank|>
        // <discordUser> <setPrimaryRank> <rank>
        // <discordUser> <ban>
        // <discordUser> <unBan>
        // <discordUser> <info>

        final User user = this.getDiscordUser(commandParameters, 0);
        final UserDb userDb = UserDb.getOrCreate(user.getIdLong());
        final String arg1 = this.getFromListIgnoreCase(commandParameters, 1, VALID_1_ARGS);

        switch (arg1) {
            case "info":
                return this.infoCommand(commandParameters, userDb);
            case "unBan":
                return this.unBanCommand(commandParameters, userDb, user);
            case "ban":
                return this.banCommand(commandParameters, userDb, user);
            case "setPrimaryRank":
                return this.setPrimaryRankCommand(commandParameters, userDb);
            case "rank":
                return this.rankCommand(commandParameters, userDb);
            case "perms":
                return this.permsCommand(commandParameters, userDb, user);
            case "setting":
                return this.settingCommand(commandParameters, userDb);
            case "achievement":
                return this.achievementCommand(commandParameters, userDb);
            default:
                return CommandResult.ERROR;
        }
    }

    private CommandResult infoCommand(final CommandParameters commandParameters, final UserDb userDb) {
        final int commandSpamCache = StatsBot.getCommandManager().getCommandSpamCache().get(userDb.getDiscordId()).get();
        final int activeEmotes = StatsBot.getEmoteReactionManager().getActiveEmotesPerPlayer().getOrDefault(userDb.getDiscordId(), new AtomicInteger(0)).get();

        final String settings = userDb.getSettingsMap()
                .entrySet()
                .stream()
                .map(setting -> setting.getKey().getInternalName() + ": " + setting.getKey().parseSetting(setting.getValue()))
                .collect(Collectors.joining("\n"));

        final String stats = userDb.getStatsMap()
                .entrySet()
                .stream()
                .map(setting -> setting.getKey().getInternalName() + ": " + setting.getValue())
                .collect(Collectors.joining("\n"));

        final RankManager rankManager = StatsBot.getRankManager();
        final String primaryRank = rankManager.getRank(userDb.getPrimaryRank())
                .map(Rank::getName)
                .orElse("Unknown");
        final String subRanks = userDb.getRanks()
                .stream()
                .map(rankManager::getRank)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Rank::getName)
                .collect(Collectors.joining("; "));

        final String permissions = userDb.getPermissionIds()
                .stream()
                .map(StatsBot.getPermissionsManager()::getPermissionFromId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining("\n"));

        final String allPermissions = userDb.getAllPermissionIds()
                .stream()
                .map(StatsBot.getPermissionsManager()::getPermissionFromId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining("\n"));

        this.sendTimedMessage(commandParameters,
                this.getEmbedBuilder(commandParameters)
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
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Error")
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
                            .setTitle("Error")
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
        if (3 > commandParameters.getArgs().length) {
            this.sendMissingArgsMessage(commandParameters, 3);
            return CommandResult.MISSING_ARGS;
        }

        final Rank rank = this.getRank(commandParameters, 2);
        if (userDb.hasPrimaryRank(rank)) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Error")
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
                        .setDescription("Set primary rank to " + MarkdownUtil.monospace(rank.getName()) + "."),
                90
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult rankCommand(final CommandParameters commandParameters, final UserDb userDb) {
        if (4 > commandParameters.getArgs().length) {
            this.sendMissingArgsMessage(commandParameters, 4);
            return CommandResult.MISSING_ARGS;
        }

        final String mode = this.getFromListIgnoreCase(commandParameters, 2, RANK_2_ARGS);
        final Rank rank = this.getRank(commandParameters, 3);

        if ("add".equalsIgnoreCase(mode)) {
            if (userDb.hasRank(rank)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
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
                            .setDescription("Added " + MarkdownUtil.monospace(rank.getName()) + " rank to the user."),
                    90
            );

        } else if ("remove".equalsIgnoreCase(mode)) {
            if (!userDb.hasRank(rank)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
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
                            .setDescription("Removed " + MarkdownUtil.monospace(rank.getName()) + " rank from the user."),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult permsCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        if (4 > commandParameters.getArgs().length) {
            this.sendMissingArgsMessage(commandParameters, 4);
            return CommandResult.MISSING_ARGS;
        }

        final String mode = this.getFromListIgnoreCase(commandParameters, 2, PERMS_2_ARGS);
        final int permissionId = this.getPermissionId(commandParameters, 3);
        final String permissionNode = StatsBot.getPermissionsManager().getPermissionFromId(permissionId)
                .orElseThrow(RuntimeException::new);

        if ("add".equalsIgnoreCase(mode)) {
            if (userDb.hasPermission(permissionId)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " does already possess the " + MarkdownUtil.monospace(permissionNode) + " permission."),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.addPermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Added Permission")
                            .setDescription(MarkdownUtil.monospace(permissionNode) + " added to " + MarkdownUtil.monospace(discordUser.getAsTag())),
                    90
            );

        } else if ("remove".equalsIgnoreCase(mode)) {
            if (!userDb.hasPermission(permissionId)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " does not possess the " + MarkdownUtil.monospace(permissionNode) + " permission."),
                        90
                );

                return CommandResult.FAIL;
            }

            userDb.removePermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Removed Permission")
                            .setDescription(MarkdownUtil.monospace(permissionNode) + " removed from " + MarkdownUtil.monospace(discordUser.getAsTag())),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult settingCommand(final CommandParameters commandParameters, final UserDb userDb) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Not implemented")
                        .setDescription("This is currently not implemented"),
                90
        );
        return CommandResult.SUCCESS;
    }

    private CommandResult achievementCommand(final CommandParameters commandParameters, final UserDb userDb) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Not implemented")
                        .setDescription("This is currently not implemented"),
                90
        );
        return CommandResult.SUCCESS;
    }

    // Utilities
    private Rank getRank(final CommandParameters commandParameters, final int position) {
        final String userInput = commandParameters.getArgs()[position];

        return StatsBot.getRankManager().getRanks()
                .stream()
                .filter(rank -> rank.getName().equalsIgnoreCase(userInput))
                .findAny()
                .orElseThrow(() -> new CommandReturnException(
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(userInput) + " is not a valid rank.")
                ));
    }
}
