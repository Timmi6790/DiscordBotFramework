package de.timmi6790.discord_framework.module.modules.rank.commands;

import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import org.jetbrains.annotations.Nullable;

public class RankCommand extends SlashCommand {
    private static final String ERROR_TITLE = "Error";

    private final RankModule rankModule;
    @Nullable
    private final SettingModule settingsModule;

    public RankCommand(final RankModule rankModule,
                       @Nullable final SettingModule settingsModule,
                       final SlashCommandModule module) {
        // TODO: Add a better command system, to support more complex commands
        super(module, "rank", "Rank control");

        this.addProperties(
                new CategoryProperty("Management"),
                new SyntaxProperty("<rankName|list> <perms;extend;rename;rename;create;delete;info|> <add,remove;add,remove;newName|> <command,permNode;rankName|>")
        );

        this.rankModule = rankModule;
        this.settingsModule = settingsModule;
    }

    /*
    private int getPermissionIdOrThrow(final CommandParameters commandParameters, final int argPosition) {
        return SlashArgumentUtilities.getPermissionIdOrThrow(
                commandParameters,
                argPosition,
                this.getCommandModule(),
                this.settingsModule,
                this.getPermissionsModule()
        );
    }

     */

    @Override
    protected CommandResult onCommand(final SlashCommandParameters commandParameters) {
        // <list>
        // <rankName> <perms> <add|remove> <command|permNode>
        // <rankName> <extend> <add|remove> <rankName>
        // <rankName> <rename> <newName>
        // <rankName> <create>
        // <rankName> <delete>
        // <rankName> <info>

        // List command
        return BaseCommandResult.SUCCESSFUL;
    }
        /*
        final String arg0 = commandParameters.getArg(0);
        if ("list".equalsIgnoreCase(arg0)) {
            return this.listCommand(commandParameters);
        }

        // All further commands require a minimum length of 2
        this.checkArgLength(commandParameters, 2);

        // Special handling for create
        final ValidArgs1 args1 = SlashArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                this.getClass(),
                1,
                ValidArgs1.class
        );
        if (commandParameters.getArgs().length >= 2 && args1 == ValidArgs1.CREATE) {
            return this.createRankCommand(commandParameters, arg0);
        }

        // All other commands
        final Rank rank = SlashArgumentUtilities.getRankOrThrow(commandParameters, 0, this.rankModule);
        return switch (args1) {
            case INFO -> this.infoCommand(commandParameters, rank);
            case DELETE -> this.deleteRankCommand(commandParameters, rank);
            case RENAME -> this.renameRankCommand(commandParameters, rank);
            case EXTEND -> this.extendCommand(commandParameters, rank);
            case PERMS -> this.permsCommand(commandParameters, rank);
            default -> BaseCommandResult.ERROR;
        };
    }

    // Sub commands
    private CommandResult permsCommand(final CommandParameters commandParameters, final Rank rank) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = SlashArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                this.getClass(),
                2,
                AddRemoveArgs.class
        );
        final int permissionId = this.getPermissionIdOrThrow(commandParameters, 3);
        final String permissionNode = this.getPermissionsModule().getPermissionFromId(permissionId)
                .orElseThrow(RuntimeException::new);

        if (AddRemoveArgs.ADD == mode) {
            // Check if the rank already has this permission
            if (rank.hasPermission(permissionId, false)) {
                commandParameters.sendMessage(
                        commandParameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s does already possess the %s permission.",
                                        MarkdownUtil.monospace(rank.getRankName()),
                                        MarkdownUtil.monospace(permissionNode)
                                )
                );
                return BaseCommandResult.FAIL;
            }

            // Add new permission to rank
            rank.addPermission(permissionId);
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Added Permission")
                            .setDescription(
                                    "%s added to %s.",
                                    MarkdownUtil.monospace(permissionNode),
                                    MarkdownUtil.monospace(rank.getRankName())
                            )
            );
            return BaseCommandResult.SUCCESSFUL;

        } else if (AddRemoveArgs.REMOVE == mode) {
            // Check if the rank has the permission
            if (!rank.hasPermission(permissionId, false)) {
                commandParameters.sendMessage(
                        commandParameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s does not possess the %s permission.",
                                        MarkdownUtil.monospace(rank.getRankName()),
                                        MarkdownUtil.monospace(permissionNode)
                                )
                );
                return BaseCommandResult.FAIL;
            }

            // Remove permission from rank
            rank.removePermission(permissionId);
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Removed Permission")
                            .setDescription(
                                    "%s removed from %s.",
                                    MarkdownUtil.monospace(permissionNode),
                                    MarkdownUtil.monospace(rank.getRankName())
                            )
            );
            return BaseCommandResult.SUCCESSFUL;
        }

        return BaseCommandResult.ERROR;
    }

    private CommandResult extendCommand(final CommandParameters commandParameters, final Rank rank) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = SlashArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                this.getClass(),
                2,
                AddRemoveArgs.class
        );
        final Rank extendedRank = SlashArgumentUtilities.getRankOrThrow(commandParameters, 3, this.rankModule);

        // Can't target the same rank
        if (rank.getRepositoryId() == extendedRank.getRepositoryId()) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle(ERROR_TITLE)
                            .setDescription("You can't target the same rank.")
            );
            return BaseCommandResult.INVALID_ARGS;
        }

        if (AddRemoveArgs.ADD == mode) {
            if (rank.hasExtendedRank(extendedRank)) {
                commandParameters.sendMessage(
                        commandParameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s is already extending %s.",
                                        MarkdownUtil.monospace(rank.getRankName()),
                                        MarkdownUtil.monospace(extendedRank.getRankName())
                                )
                );
                return BaseCommandResult.INVALID_ARGS;
            }

            rank.addExtendedRank(extendedRank);
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle(rank.getRankName() + " - Extended Rank")
                            .setDescription(
                                    "%s added to %s.",
                                    MarkdownUtil.monospace(extendedRank.getRankName()),
                                    MarkdownUtil.monospace(rank.getRankName())
                            )
            );
        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!rank.hasExtendedRank(extendedRank)) {
                commandParameters.sendMessage(
                        commandParameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s is not extending %s.",
                                        MarkdownUtil.monospace(rank.getRankName()),
                                        MarkdownUtil.monospace(extendedRank.getRankName())
                                )
                );
                return BaseCommandResult.INVALID_ARGS;
            }

            rank.removeExtendedRank(extendedRank);
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle(rank.getRankName() + " - Extended Rank")
                            .setDescription(
                                    "%s removed from %s.",
                                    MarkdownUtil.monospace(extendedRank.getRankName()),
                                    MarkdownUtil.monospace(rank.getRankName())
                            )
            );
        }

        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult renameRankCommand(final CommandParameters commandParameters, final Rank rank) {
        this.checkArgLength(commandParameters, 3);

        final String oldName = rank.getRankName();
        final String newName = commandParameters.getArg(2);
        rank.setRankName(newName);

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle(rank.getRankName())
                        .setDescription(
                                "%s changed to %s.",
                                MarkdownUtil.monospace(oldName),
                                MarkdownUtil.monospace(newName)
                        )
        );
        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult createRankCommand(final CommandParameters commandParameters, final String rankName) {
        final boolean success = this.rankModule.createRank(rankName);
        if (success) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Created")
                            .setDescription(
                                    "Successfully created %s.",
                                    MarkdownUtil.monospace(rankName)
                            )
            );
            return BaseCommandResult.SUCCESSFUL;
        } else {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle(ERROR_TITLE)
                            .setDescription(
                                    "Something went wrong while creating %s.",
                                    MarkdownUtil.monospace(rankName)
                            )
            );
            return BaseCommandResult.ERROR;
        }
    }

    private CommandResult deleteRankCommand(final CommandParameters commandParameters, final Rank rank) {
        final boolean isRankDeleted = this.rankModule.deleteRank(rank);
        if (isRankDeleted) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Deleted")
                            .setDescription(
                                    "Successfully deleted %s[%s].",
                                    MarkdownUtil.monospace(rank.getRankName()),
                                    MarkdownUtil.monospace(String.valueOf(rank.getRepositoryId()))
                            )
            );
            return BaseCommandResult.SUCCESSFUL;
        } else {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle(ERROR_TITLE)
                            .setDescription(
                                    "Something went wrong while deleting %s[%s].",
                                    MarkdownUtil.monospace(rank.getRankName()),
                                    MarkdownUtil.monospace(String.valueOf(rank.getRepositoryId()))
                            )
            );
            return BaseCommandResult.ERROR;
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

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle(rank.getRankName() + " - Rank")
                        .addField("DatabaseId", String.valueOf(rank.getRepositoryId()), true)
                        .addField("Name", rank.getRankName(), true)
                        .addField("ExtendedRanks", String.join("\n", extendedRank), true)
                        .addField("ExtendedPerms", String.join("\n", extendedPerms), false)
                        .addField("Perms", String.join("\n", perms), false)
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult listCommand(final CommandParameters commandParameters) {
        final List<String> rankNames = DataUtilities.convertToStringList(this.rankModule.getRanks(), Rank::getRankName);
        rankNames.sort(Comparator.naturalOrder());

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Ranks")
                        .setDescription(String.join("\n", rankNames))
        );
        return BaseCommandResult.SUCCESSFUL;
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

         */
}
