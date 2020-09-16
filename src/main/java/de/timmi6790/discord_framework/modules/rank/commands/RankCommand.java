package de.timmi6790.discord_framework.modules.rank.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.rank.Rank;
import de.timmi6790.discord_framework.modules.rank.RankModule;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.stream.Collectors;

public class RankCommand extends AbstractCommand<RankModule> {
    private static final String ERROR_TITLE = "Error";
    private static final String FALLBACK_NAME = "Unknown";

    public RankCommand() {
        // TODO: Add a better command system, to support more complex commands
        super("rank", "Management", "Rank control command", "<rankName|list> <perms;extend;rename;rename;create;delete;info|> <add,remove;add,remove;newName|> <command,permNode;rankName|>");

        this.addProperty(
                new MinArgCommandProperty(1)
        );
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

        this.checkArgLength(commandParameters, 2);

        // Special handling for create
        final ValidArgs1 args1 = this.getFromEnumIgnoreCaseThrow(commandParameters, 1, ValidArgs1.values());
        if (commandParameters.getArgs().length >= 2 && args1 == ValidArgs1.CREATE) {
            return this.createCommand(commandParameters, commandParameters.getArgs()[0]);
        }

        // All other commands
        final Rank rank = this.getRankThrow(commandParameters, 0);
        this.checkArgLength(commandParameters, 2);

        switch (args1) {
            case INFO:
                return this.infoCommand(commandParameters, rank);
            case DELETE:
                return this.deleteCommand(commandParameters, rank);
            case RENAME:
                return this.renameCommand(commandParameters, rank);
            case EXTEND:
                return this.extendCommand(commandParameters, rank);
            case PERMS:
                return this.permsCommand(commandParameters, rank);
            default:
                return CommandResult.ERROR;
        }
    }

    // Sub commands
    private CommandResult permsCommand(final CommandParameters commandParameters, final Rank rank) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = this.getFromEnumIgnoreCaseThrow(commandParameters, 2, AddRemoveArgs.values());
        final int permissionId = this.getPermissionIdThrow(commandParameters, 3);
        final String permissionNode = this.getModule().getModuleOrThrow(PermissionsModule.class).getPermissionFromId(permissionId)
                .orElseThrow(RuntimeException::new);

        if (AddRemoveArgs.ADD == mode) {
            if (rank.hasPermission(permissionId)) {
                sendTimedMessage(
                        commandParameters,
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(MarkdownUtil.monospace(rank.getName()) + " does already possess the " + MarkdownUtil.monospace(permissionNode) + " permission."),
                        90
                );

                return CommandResult.FAIL;
            }

            rank.addPermission(permissionId);
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Added Permission")
                            .setDescription(MarkdownUtil.monospace(permissionNode) + " added to " + MarkdownUtil.monospace(rank.getName())),
                    90
            );

            return CommandResult.SUCCESS;

        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!rank.hasPermission(permissionId)) {
                sendTimedMessage(
                        commandParameters,
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(MarkdownUtil.monospace(rank.getName()) + " does not possess the " + MarkdownUtil.monospace(permissionNode) + " permission."),
                        90
                );

                return CommandResult.FAIL;
            }

            rank.removePermission(permissionId);
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Removed Permission")
                            .setDescription(MarkdownUtil.monospace(permissionNode) + " removed from " + MarkdownUtil.monospace(rank.getName())),
                    90
            );

            return CommandResult.SUCCESS;
        }

        return CommandResult.ERROR;
    }

    private CommandResult extendCommand(final CommandParameters commandParameters, final Rank rank) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = this.getFromEnumIgnoreCaseThrow(commandParameters, 2, AddRemoveArgs.values());
        final Rank extendedRank = this.getRankThrow(commandParameters, 3);

        // Can't target the same rank
        if (rank.getDatabaseId() == extendedRank.getDatabaseId()) {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription("You can't target the same rank"),
                    90
            );
            return CommandResult.INVALID_ARGS;
        }

        if (AddRemoveArgs.ADD == mode) {
            if (rank.hasExtendedRank(extendedRank)) {
                sendTimedMessage(
                        commandParameters,
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(MarkdownUtil.monospace(rank.getName()) + " is already extending " + MarkdownUtil.monospace(extendedRank.getName())),
                        90
                );
                return CommandResult.INVALID_ARGS;
            }

            rank.addExtendedRank(extendedRank);
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle(rank.getName() + " - Extended Rank")
                            .setDescription(MarkdownUtil.monospace(extendedRank.getName()) + " added to " + MarkdownUtil.monospace(rank.getName())),
                    90
            );
        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!rank.hasExtendedRank(extendedRank)) {
                sendTimedMessage(
                        commandParameters,
                        getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(MarkdownUtil.monospace(rank.getName()) + " is not extending " + MarkdownUtil.monospace(extendedRank.getName())),
                        90
                );
                return CommandResult.INVALID_ARGS;
            }

            rank.removeExtendedRank(extendedRank);
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle(rank.getName() + " - Extended Rank")
                            .setDescription(MarkdownUtil.monospace(extendedRank.getName()) + " removed from " + MarkdownUtil.monospace(rank.getName())),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult renameCommand(final CommandParameters commandParameters, final Rank rank) {
        this.checkArgLength(commandParameters, 3);

        final String oldName = rank.getName();
        final String newName = commandParameters.getArgs()[2];
        rank.setName(newName);

        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle(rank.getName())
                        .setDescription(MarkdownUtil.monospace(oldName) + " changed to " + MarkdownUtil.monospace(newName)),
                90
        );
        return CommandResult.SUCCESS;
    }

    private CommandResult createCommand(final CommandParameters commandParameters, final String rankName) {
        final boolean success = this.getModule().getModuleOrThrow(RankModule.class).createRank(rankName);
        if (success) {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Created")
                            .setDescription("Successfully created " + MarkdownUtil.monospace(rankName) + "."),
                    90
            );
        } else {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Created")
                            .setDescription("Something went wrong while creating " + MarkdownUtil.monospace(rankName) + "."),
                    90
            );
        }
        return CommandResult.SUCCESS;
    }

    private CommandResult deleteCommand(final CommandParameters commandParameters, final Rank rank) {
        final boolean success = this.getModule().getModuleOrThrow(RankModule.class).deleteRank(rank);
        if (success) {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Deleted")
                            .setDescription("Successfully deleted " + MarkdownUtil.monospace(rank.getName() + "[" + rank.getDatabaseId() + "]") + "."),
                    90
            );
        } else {
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription("Something went wrong while deleting " + MarkdownUtil.monospace(rank.getName() + "[" + rank.getDatabaseId() + "]") + "."),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult infoCommand(final CommandParameters commandParameters, final Rank rank) {
        final RankModule rankModule = this.getModule().getModuleOrThrow(RankModule.class);
        final String extendedRanks = rank.getExtendedRanks()
                .stream()
                .map(rankId ->
                        rankModule.getRank(rankId)
                                .map(Rank::getName)
                                .orElse(FALLBACK_NAME) + "[" + rankId + "]"
                )
                .collect(Collectors.joining("\n"));

        final PermissionsModule permissionsModule = this.getModule().getModuleOrThrow(PermissionsModule.class);
        final String perms = rank.getPermissions()
                .stream()
                .map(permId ->
                        permissionsModule.getPermissionFromId(permId).orElse(FALLBACK_NAME) + "[" + permId + "]"
                )
                .collect(Collectors.joining("\n"));

        final String extendedPerms = rank.getAllPermissions()
                .stream()
                .filter(permId -> !rank.getPermissions().contains(permId))
                .map(permId ->
                        permissionsModule.getPermissionFromId(permId).orElse(FALLBACK_NAME) + "[" + permId + "]"
                )
                .collect(Collectors.joining("\n"));

        final String userCount = String.valueOf(rank.retrievePlayerCount());

        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
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
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Ranks")
                        .setDescription(
                                this.getModule().getModuleOrThrow(RankModule.class).getRanks()
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
    private enum ValidArgs1 {
        PERMS,
        EXTEND,
        RENAME,
        CREATE,
        DELETE,
        INFO
    }

    private enum AddRemoveArgs {
        ADD,
        REMOVE
    }
}
