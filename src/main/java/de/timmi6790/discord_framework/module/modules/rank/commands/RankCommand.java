package de.timmi6790.discord_framework.module.modules.rank.commands;

import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankCommand extends AbstractCommand {
    private static final String ERROR_TITLE = "Error";

    public RankCommand() {
        // TODO: Add a better command system, to support more complex commands
        super(
                "rank",
                "Management",
                "Rank control command",
                "<rankName|list> <perms;extend;rename;rename;create;delete;info|> <add,remove;add,remove;newName|> <command,permNode;rankName|>"
        );

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
        final String arg0 = this.getArg(commandParameters, 0);
        if ("list".equalsIgnoreCase(arg0)) {
            return this.listCommand(commandParameters);
        }

        // All further commands require a minimum length of 2
        this.checkArgLength(commandParameters, 2);

        // Special handling for create
        final ValidArgs1 args1 = this.getFromEnumIgnoreCaseThrow(commandParameters, 1, ValidArgs1.values());
        if (commandParameters.getArgs().length >= 2 && args1 == ValidArgs1.CREATE) {
            return this.createRankCommand(commandParameters, arg0);
        }

        // All other commands
        final Rank rank = this.getRankThrow(commandParameters, 0);
        return switch (args1) {
            case INFO -> this.infoCommand(commandParameters, rank);
            case DELETE -> this.deleteRankCommand(commandParameters, rank);
            case RENAME -> this.renameRankCommand(commandParameters, rank);
            case EXTEND -> this.extendCommand(commandParameters, rank);
            case PERMS -> this.permsCommand(commandParameters, rank);
            default -> CommandResult.ERROR;
        };
    }

    // Sub commands
    private CommandResult permsCommand(final CommandParameters commandParameters, final Rank rank) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = this.getFromEnumIgnoreCaseThrow(commandParameters, 2, AddRemoveArgs.values());
        final int permissionId = this.getPermissionIdThrow(commandParameters, 3);
        final String permissionNode = this.getPermissionsModule().getPermissionFromId(permissionId)
                .orElseThrow(RuntimeException::new);

        if (AddRemoveArgs.ADD == mode) {
            // Check if the rank already has this permission
            if (rank.hasPermission(permissionId, false)) {
                this.sendTimedMessage(
                        commandParameters,
                        ERROR_TITLE,
                        "%s does already possess the %s permission.",
                        MarkdownUtil.monospace(rank.getRankName()),
                        MarkdownUtil.monospace(permissionNode)
                );

                return CommandResult.FAIL;
            }

            // Add new permission to rank
            rank.addPermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    "Added Permission",
                    "%s added to %s.",
                    MarkdownUtil.monospace(permissionNode),
                    MarkdownUtil.monospace(rank.getRankName())
            );

            return CommandResult.SUCCESS;

        } else if (AddRemoveArgs.REMOVE == mode) {
            // Check if the rank has the permission
            if (!rank.hasPermission(permissionId, false)) {
                this.sendTimedMessage(
                        commandParameters,
                        ERROR_TITLE,
                        "%s does not possess the %s permission.",
                        MarkdownUtil.monospace(rank.getRankName()),
                        MarkdownUtil.monospace(permissionNode)
                );

                return CommandResult.FAIL;
            }

            // Remove permission from rank
            rank.removePermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    "Removed Permission",
                    "%s removed from %s.",
                    MarkdownUtil.monospace(permissionNode),
                    MarkdownUtil.monospace(rank.getRankName())
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
        if (rank.getRepositoryId() == extendedRank.getRepositoryId()) {
            this.sendTimedMessage(
                    commandParameters,
                    ERROR_TITLE,
                    "You can't target the same rank."
            );

            return CommandResult.INVALID_ARGS;
        }

        if (AddRemoveArgs.ADD == mode) {
            if (rank.hasExtendedRank(extendedRank)) {
                this.sendTimedMessage(
                        commandParameters,
                        ERROR_TITLE,
                        "%s is already extending %s.",
                        MarkdownUtil.monospace(rank.getRankName()),
                        MarkdownUtil.monospace(extendedRank.getRankName())
                );

                return CommandResult.INVALID_ARGS;
            }

            rank.addExtendedRank(extendedRank);
            this.sendTimedMessage(
                    commandParameters,
                    rank.getRankName() + " - Extended Rank",
                    "%s added to %s.",
                    MarkdownUtil.monospace(extendedRank.getRankName()),
                    MarkdownUtil.monospace(rank.getRankName())
            );
        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!rank.hasExtendedRank(extendedRank)) {
                this.sendTimedMessage(
                        commandParameters,
                        ERROR_TITLE,
                        "%s is not extending %s.",
                        MarkdownUtil.monospace(rank.getRankName()),
                        MarkdownUtil.monospace(extendedRank.getRankName())
                );

                return CommandResult.INVALID_ARGS;
            }

            rank.removeExtendedRank(extendedRank);
            this.sendTimedMessage(
                    commandParameters,
                    rank.getRankName() + " - Extended Rank",
                    "%s removed from %s.",
                    MarkdownUtil.monospace(extendedRank.getRankName()),
                    MarkdownUtil.monospace(rank.getRankName())
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult renameRankCommand(final CommandParameters commandParameters, final Rank rank) {
        this.checkArgLength(commandParameters, 3);

        final String oldName = rank.getRankName();
        final String newName = this.getArg(commandParameters, 2);
        rank.setRankName(newName);

        this.sendTimedMessage(
                commandParameters,
                rank.getRankName(),
                "%s changed to %s.",
                MarkdownUtil.monospace(oldName),
                MarkdownUtil.monospace(newName)
        );
        return CommandResult.SUCCESS;
    }

    private CommandResult createRankCommand(final CommandParameters commandParameters, final String rankName) {
        final boolean success = this.getRankModule().createRank(rankName);
        if (success) {
            this.sendTimedMessage(
                    commandParameters,
                    "Created",
                    "Successfully created %s.",
                    MarkdownUtil.monospace(rankName)
            );

            return CommandResult.SUCCESS;
        } else {
            this.sendTimedMessage(
                    commandParameters,
                    ERROR_TITLE,
                    "Something went wrong while creating %s.",
                    MarkdownUtil.monospace(rankName)
            );

            return CommandResult.ERROR;
        }
    }

    private CommandResult deleteRankCommand(final CommandParameters commandParameters, final Rank rank) {
        final boolean isRankDeleted = this.getRankModule().deleteRank(rank);
        if (isRankDeleted) {
            this.sendTimedMessage(
                    commandParameters,
                    "Deleted",
                    "Successfully deleted %s[%s].",
                    MarkdownUtil.monospace(rank.getRankName()),
                    MarkdownUtil.monospace(String.valueOf(rank.getRepositoryId()))
            );

            return CommandResult.SUCCESS;
        } else {
            this.sendTimedMessage(
                    commandParameters,
                    ERROR_TITLE,
                    "Something went wrong while deleting %s[%s].",
                    MarkdownUtil.monospace(rank.getRankName()),
                    MarkdownUtil.monospace(String.valueOf(rank.getRepositoryId()))
            );

            return CommandResult.ERROR;
        }
    }

    private CommandResult infoCommand(final CommandParameters commandParameters, final Rank rank) {
        final List<String> extendedRank = DataUtilities.convertToStringList(rank.getExtendedRanks(), Rank::getRankName);
        extendedRank.sort(Comparator.naturalOrder());

        final List<String> perms = new ArrayList<>(rank.getPermissions(false));
        perms.sort(Comparator.naturalOrder());

        final List<String> extendedPerms = new ArrayList<>();
        for (final String perm : rank.getPermissions(true)) {
            if (!perms.contains(perm)) {
                extendedPerms.add(perm);
            }
        }
        extendedPerms.sort(Comparator.naturalOrder());

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(rank.getRankName() + " - Rank")
                        .addField("DatabaseId", String.valueOf(rank.getRepositoryId()), true)
                        .addField("Name", rank.getRankName(), true)
                        .addField("ExtendedRanks", String.join("\n", extendedRank), true)
                        .addField("ExtendedPerms", String.join("\n", extendedPerms), false)
                        .addField("Perms", String.join("\n", perms), false),
                150
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult listCommand(final CommandParameters commandParameters) {
        final List<String> rankNames = DataUtilities.convertToStringList(this.getRankModule().getRanks(), Rank::getRankName);
        rankNames.sort(Comparator.naturalOrder());

        this.sendTimedMessage(
                commandParameters,
                "Ranks",
                String.join("\n", rankNames)
        );
        return CommandResult.SUCCESS;
    }

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
