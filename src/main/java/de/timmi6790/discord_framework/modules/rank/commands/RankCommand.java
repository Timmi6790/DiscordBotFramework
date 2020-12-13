package de.timmi6790.discord_framework.modules.rank.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.rank.Rank;
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
        final String permissionNode = this.getPermissionsModule().getPermissionFromId(permissionId)
                .orElseThrow(RuntimeException::new);

        if (AddRemoveArgs.ADD == mode) {
            if (rank.hasPermission(permissionId)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s does already possess the %s permission.",
                                        MarkdownUtil.monospace(rank.getName()),
                                        MarkdownUtil.monospace(permissionNode)
                                ),
                        90
                );

                return CommandResult.FAIL;
            }

            rank.addPermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Added Permission")
                            .setDescription(
                                    "%s added to %s.",
                                    MarkdownUtil.monospace(permissionNode),
                                    MarkdownUtil.monospace(rank.getName())
                            ),
                    90
            );

            return CommandResult.SUCCESS;

        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!rank.hasPermission(permissionId)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s does not possess the %s permission.",
                                        MarkdownUtil.monospace(rank.getName()),
                                        MarkdownUtil.monospace(permissionNode)
                                ),
                        90
                );

                return CommandResult.FAIL;
            }

            rank.removePermission(permissionId);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Removed Permission")
                            .setDescription(
                                    "%s removed from %s.",
                                    MarkdownUtil.monospace(permissionNode),
                                    MarkdownUtil.monospace(rank.getName())
                            ),
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
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription("You can't target the same rank."),
                    90
            );
            return CommandResult.INVALID_ARGS;
        }

        if (AddRemoveArgs.ADD == mode) {
            if (rank.hasExtendedRank(extendedRank)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s is already extending %s.",
                                        MarkdownUtil.monospace(rank.getName()),
                                        MarkdownUtil.monospace(extendedRank.getName())
                                ),
                        90
                );
                return CommandResult.INVALID_ARGS;
            }

            rank.addExtendedRank(extendedRank);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle(rank.getName() + " - Extended Rank")
                            .setDescription(
                                    "%s added to %s.",
                                    MarkdownUtil.monospace(extendedRank.getName()),
                                    MarkdownUtil.monospace(rank.getName())
                            ),
                    90
            );
        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!rank.hasExtendedRank(extendedRank)) {
                this.sendTimedMessage(
                        commandParameters,
                        this.getEmbedBuilder(commandParameters)
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s is not extending %s.",
                                        MarkdownUtil.monospace(rank.getName()),
                                        MarkdownUtil.monospace(extendedRank.getName())
                                ),
                        90
                );
                return CommandResult.INVALID_ARGS;
            }

            rank.removeExtendedRank(extendedRank);
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle(rank.getName() + " - Extended Rank")
                            .setDescription(
                                    "%s removed from %s.",
                                    MarkdownUtil.monospace(extendedRank.getName()),
                                    MarkdownUtil.monospace(rank.getName())
                            ),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult renameCommand(final CommandParameters commandParameters, final Rank rank) {
        this.checkArgLength(commandParameters, 3);

        final String oldName = rank.getName();
        final String newName = this.getArg(commandParameters, 2);
        rank.setName(newName);

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(rank.getName())
                        .setDescription(
                                "%s changed to %s.",
                                MarkdownUtil.monospace(oldName),
                                MarkdownUtil.monospace(newName)
                        ),
                90
        );
        return CommandResult.SUCCESS;
    }

    private CommandResult createCommand(final CommandParameters commandParameters, final String rankName) {
        final boolean success = this.getRankModule().createRank(rankName);
        if (success) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Created")
                            .setDescription(
                                    "Successfully created %s.",
                                    MarkdownUtil.monospace(rankName)
                            ),
                    90
            );
        } else {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Created")
                            .setDescription(
                                    "Something went wrong while creating %s.",
                                    MarkdownUtil.monospace(rankName)
                            ),
                    90
            );
        }
        return CommandResult.SUCCESS;
    }

    private CommandResult deleteCommand(final CommandParameters commandParameters, final Rank rank) {
        final boolean success = this.getRankModule().deleteRank(rank);
        if (success) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Deleted")
                            .setDescription(
                                    "Successfully deleted %s[%s].",
                                    MarkdownUtil.monospace(rank.getName()),
                                    MarkdownUtil.monospace(String.valueOf(rank.getDatabaseId()))
                            ),
                    90
            );
        } else {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle(ERROR_TITLE)
                            .setDescription(
                                    "Something went wrong while deleting %s[%s].",
                                    MarkdownUtil.monospace(rank.getName()),
                                    MarkdownUtil.monospace(String.valueOf(rank.getDatabaseId()))
                            ),
                    90
            );
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult infoCommand(final CommandParameters commandParameters, final Rank rank) {
        final List<String> extendedRank = DataUtilities.convertToStringList(rank.getExtendedRanks(), Rank::getName);
        extendedRank.sort(Comparator.naturalOrder());

        final List<String> perms = new ArrayList<>(rank.getPermissions());
        perms.sort(Comparator.naturalOrder());

        final List<String> extendedPerms = new ArrayList<>();
        for (final String perm : rank.getAllPermissions()) {
            if (!perms.contains(perm)) {
                extendedPerms.add(perm);
            }
        }
        extendedPerms.sort(Comparator.naturalOrder());

        final String userCount = String.valueOf(rank.retrievePlayerCount());

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(rank.getName() + " - Rank")
                        .addField("DatabaseId", String.valueOf(rank.getDatabaseId()), true)
                        .addField("Name", rank.getName(), true)
                        .addField("User Count", userCount, true)
                        .addField("ExtendedRanks", String.join("\n", extendedRank), true)
                        .addField("ExtendedPerms", String.join("\n", extendedPerms), false)
                        .addField("Perms", String.join("\n", perms), false),
                150
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult listCommand(final CommandParameters commandParameters) {
        final List<String> rankNames = DataUtilities.convertToStringList(this.getRankModule().getRanks(), Rank::getName);
        rankNames.sort(Comparator.naturalOrder());

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Ranks")
                        .setDescription(String.join("\n", rankNames)),
                100
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
