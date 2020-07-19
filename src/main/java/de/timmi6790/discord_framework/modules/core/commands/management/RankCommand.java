package de.timmi6790.discord_framework.modules.core.commands.management;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsManager;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankManager;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RankCommand extends AbstractCommand {
    private static final List<String> VALID_1_ARGS = Arrays.asList("perms", "extend", "rename", "create", "delete", "info");
    private static final List<String> PERMS_2_ARGS = Arrays.asList("add", "remove");
    private static final List<String> EXTEND_2_ARGS = Arrays.asList("add", "remove");

    public RankCommand() {
        // TODO: Add a better command system, to support more complex commands
        super("rank", "Management", "Rank control command", "<rankName|list> <perms;extend;rename;rename;create;delete;info|> <add,remove;add,remove;newName|> <command,permNode;rankName|>");

        this.setMinArgs(1);
        this.setPermission("core.management.rank_control");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // <list>
        // <rankName> <perms> <add|remove> <command|permNode>
        // <rankName> <extend> <add|remove> <rankName>
        // <rankName> <rename> <newName>
        // <rankName> <create>
        // <rankName> <delete>
        // <rankName> <info>

        // List command
        final String arg0 = commandParameters.getArgs()[0];
        if (arg0.equalsIgnoreCase("list")) {
            return this.listCommand(commandParameters);
        }

        // Special handling for create
        if (commandParameters.getArgs().length >= 2) {
            final String mode = commandParameters.getArgs()[1];
            if ("create".equalsIgnoreCase(mode)) {
                return this.createCommand(commandParameters, commandParameters.getArgs()[0]);
            }
        }

        // All other commands
        final Rank rank = this.getRank(commandParameters, 0);
        if (2 > commandParameters.getArgs().length) {
            this.sendMissingArgsMessage(commandParameters, 2);
            return CommandResult.MISSING_ARGS;
        }

        final String arg2 = this.getFromListIgnoreCase(commandParameters, 1, VALID_1_ARGS);
        switch (arg2) {
            case "info":
                return this.infoCommand(commandParameters, rank);
            case "delete":
                return this.deleteCommand(commandParameters, rank);
            case "rename":
                return this.renameCommand(commandParameters, rank);
            case "extend":
                return this.extendCommand(commandParameters, rank);
            case "perms":
                return this.permsCommand(commandParameters, rank);
            default:
                return CommandResult.ERROR;
        }
    }

    // Sub commands
    private CommandResult permsCommand(final CommandParameters commandParameters, final Rank rank) {
        if (4 > commandParameters.getArgs().length) {
            this.sendMissingArgsMessage(commandParameters, 4);
            return CommandResult.MISSING_ARGS;
        }

        final String mode = this.getFromListIgnoreCase(commandParameters, 2, PERMS_2_ARGS);
        final int permissionId = this.getPermissionId(commandParameters, 3);
        final String permissionNode = DiscordBot.getPermissionsManager().getPermissionFromId(permissionId)
                .orElseThrow(RuntimeException::new);

        if ("add".equalsIgnoreCase(mode)) {
            if (rank.hasPermission(permissionId)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(rank.getName()) + " does already possess the " + MarkdownUtil.monospace(permissionNode) + " permission."),
                        90
                );

                return CommandResult.FAIL;
            }

            rank.addPermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Added Permission")
                            .setDescription(MarkdownUtil.monospace(permissionNode) + " added to " + MarkdownUtil.monospace(rank.getName())),
                    90
            );

            return CommandResult.SUCCESS;

        } else if ("remove".equalsIgnoreCase(mode)) {
            if (!rank.hasPermission(permissionId)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(rank.getName()) + " does not possess the " + MarkdownUtil.monospace(permissionNode) + " permission."),
                        90
                );

                return CommandResult.FAIL;
            }

            rank.removePermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Removed Permission")
                            .setDescription(MarkdownUtil.monospace(permissionNode) + " removed from " + MarkdownUtil.monospace(rank.getName())),
                    90
            );

            return CommandResult.SUCCESS;
        }

        return CommandResult.ERROR;
    }

    private CommandResult extendCommand(final CommandParameters commandParameters, final Rank rank) {
        if (4 > commandParameters.getArgs().length) {
            this.sendMissingArgsMessage(commandParameters, 4);
            return CommandResult.MISSING_ARGS;
        }

        final String mode = this.getFromListIgnoreCase(commandParameters, 2, EXTEND_2_ARGS);
        final Rank extendedRank = this.getRank(commandParameters, 3);

        // Can't target the same rank
        if (rank.getDatabaseId() == extendedRank.getDatabaseId()) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Error")
                            .setDescription("You can't target the same rank"),
                    90
            );
            return CommandResult.INVALID_ARGS;
        }

        if ("add".equalsIgnoreCase(mode)) {
            if (rank.hasExtendedRank(extendedRank)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(rank.getName()) + " is already extending " + MarkdownUtil.monospace(extendedRank.getName())),
                        90
                );
                return CommandResult.INVALID_ARGS;
            }

            rank.addExtendedRank(extendedRank);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle(rank.getName() + " - Extended Rank")
                            .setDescription(MarkdownUtil.monospace(extendedRank.getName()) + " added to " + MarkdownUtil.monospace(rank.getName())),
                    90
            );
        } else if ("remove".equalsIgnoreCase(mode)) {
            if (!rank.hasExtendedRank(extendedRank)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(rank.getName()) + " is not extending " + MarkdownUtil.monospace(extendedRank.getName())),
                        90
                );
                return CommandResult.INVALID_ARGS;
            }

            rank.removeExtendedRank(extendedRank);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle(rank.getName() + " - Extended Rank")
                            .setDescription(MarkdownUtil.monospace(extendedRank.getName()) + " removed from " + MarkdownUtil.monospace(rank.getName())),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult renameCommand(final CommandParameters commandParameters, final Rank rank) {
        if (3 > commandParameters.getArgs().length) {
            this.sendMissingArgsMessage(commandParameters, 3);
            return CommandResult.MISSING_ARGS;
        }

        final String oldName = rank.getName();
        final String newName = commandParameters.getArgs()[2];
        rank.setName(newName);

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(rank.getName())
                        .setDescription(MarkdownUtil.monospace(oldName) + " changed to " + MarkdownUtil.monospace(newName)),
                90
        );
        return CommandResult.SUCCESS;
    }

    private CommandResult createCommand(final CommandParameters commandParameters, final String rankName) {
        final boolean success = DiscordBot.getRankManager().createRank(rankName);
        if (success) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Created")
                            .setDescription("Successfully created " + MarkdownUtil.monospace(rankName) + "."),
                    90
            );
        } else {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Created")
                            .setDescription("Something went wrong while creating " + MarkdownUtil.monospace(rankName) + "."),
                    90
            );
        }
        return CommandResult.SUCCESS;
    }

    private CommandResult deleteCommand(final CommandParameters commandParameters, final Rank rank) {
        final boolean success = DiscordBot.getRankManager().deleteRank(rank);
        if (success) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Deleted")
                            .setDescription("Successfully deleted " + MarkdownUtil.monospace(rank.getName() + "[" + rank.getDatabaseId() + "]") + "."),
                    90
            );
        } else {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Error")
                            .setDescription("Something went wrong while deleting " + MarkdownUtil.monospace(rank.getName() + "[" + rank.getDatabaseId() + "]") + "."),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult infoCommand(final CommandParameters commandParameters, final Rank rank) {
        final RankManager rankManager = DiscordBot.getRankManager();
        final String extendedRanks = rank.getExtendedRanks()
                .stream()
                .map(rankId ->
                        rankManager.getRank(rankId)
                                .map(Rank::getName)
                                .orElse("Unknown") + "[" + rankId + "]"
                )
                .collect(Collectors.joining("\n"));

        final PermissionsManager permissionsManager = DiscordBot.getPermissionsManager();
        final String perms = rank.getPermissions()
                .stream()
                .map(permId ->
                        permissionsManager.getPermissionFromId(permId).orElse("Unknown") + "[" + permId + "]"
                )
                .collect(Collectors.joining("\n"));

        final String extendedPerms = rank.getAllPermissions()
                .stream()
                .filter(permId -> !rank.getPermissions().contains(permId))
                .map(permId ->
                        permissionsManager.getPermissionFromId(permId).orElse("Unknown") + "[" + permId + "]"
                )
                .collect(Collectors.joining("\n"));

        final String userCount = String.valueOf(rank.retrievePlayerCount());

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(rank.getName() + " - Rank")
                        .addField("DatabaseId", String.valueOf(rank.getDatabaseId()), true)
                        .addField("Name", rank.getName(), true)
                        .addField("User Count", userCount, true)
                        .addField("ExtendedRanks", extendedRanks, true)
                        .addField("ExtendedPerms", extendedPerms, false)
                        .addField("Perms", perms, false),
                150
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult listCommand(final CommandParameters commandParameters) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Ranks")
                        .setDescription(
                                DiscordBot.getRankManager().getRanks()
                                        .stream()
                                        .map(Rank::getName)
                                        .sorted()
                                        .collect(Collectors.joining("\n"))
                        ),
                100
        );
        return CommandResult.SUCCESS;
    }

    // Utilities
    private Rank getRank(final CommandParameters commandParameters, final int position) {
        final String userInput = commandParameters.getArgs()[position];

        return DiscordBot.getRankManager().getRanks()
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
