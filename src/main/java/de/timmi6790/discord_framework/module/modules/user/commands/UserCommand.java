package de.timmi6790.discord_framework.module.modules.user.commands;

import de.timmi6790.discord_framework.module.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.controll.MinArgProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.command.utilities.ArgumentUtilities;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.StringJoiner;

@EqualsAndHashCode(callSuper = true)
public class UserCommand extends Command {
    private static final String ERROR_TITLE = "Error";

    private final UserDbModule userDbModule;
    @Nullable
    private final SettingModule settingsModule;
    private final RankModule rankModule;

    public UserCommand(final UserDbModule userDbModule,
                       final RankModule rankModule,
                       @Nullable final SettingModule settingsModule,
                       final CommandModule commandModule) {
        super("user", commandModule);

        this.addProperties(
                new CategoryProperty("Management"),
                new DescriptionProperty("User control command"),
                new SyntaxProperty("<discordUser> <perms|rank|setPrimaryRank|ban|unBan|info|invalidate> " +
                        "<add;remove;list|add;remove|rank|||||> <command;permNode|rank|>"),
                new AliasNamesProperty("u"),
                new MinArgProperty(2)
        );

        this.userDbModule = userDbModule;
        this.rankModule = rankModule;
        this.settingsModule = settingsModule;
    }

    private int getPermissionIdOrThrow(final CommandParameters commandParameters, final int argPosition) {
        return ArgumentUtilities.getPermissionIdOrThrow(
                commandParameters,
                argPosition,
                this.getCommandModule(),
                this.settingsModule,
                this.getPermissionsModule()
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

        final User user = ArgumentUtilities.getDiscordUserOrThrow(commandParameters, 0);
        final UserDb userDb = this.userDbModule.getOrCreate(user.getIdLong());
        final ValidArgs1 arg1 = ArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                this.getClass(),
                1,
                ValidArgs1.class
        );

        return switch (arg1) {
            case INFO -> this.infoCommand(commandParameters, userDb);
            case UN_BAN -> this.unBanCommand(commandParameters, userDb, user);
            case BAN -> this.banCommand(commandParameters, userDb, user);
            case SET_PRIMARY_RANK -> this.setPrimaryRankCommand(commandParameters, userDb);
            case RANK -> this.rankCommand(commandParameters, userDb);
            case PERMS -> this.permsCommand(commandParameters, userDb, user);
            case INVALIDATE -> this.invalidateCommand(commandParameters, userDb);
        };
    }

    private CommandResult invalidateCommand(final CommandParameters commandParameters, final UserDb userDb) {
        this.userDbModule.invalidateCache(userDb.getDiscordId());

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalidated Cache")
                        .setDescription(MarkdownUtil.monospace(String.valueOf(userDb.getDiscordId())) + " was removed from the cache.")
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult infoCommand(final CommandParameters commandParameters, final UserDb userDb) {
        final StringJoiner settings = new StringJoiner("\n");
        for (final Map.Entry<AbstractSetting<?>, String> entry : userDb.getSettings().entrySet()) {
            settings.add(entry.getKey().getInternalName() + ": " + entry.getValue());
        }

        final StringJoiner stats = new StringJoiner("\n");
        for (final Map.Entry<AbstractStat, Integer> entry : userDb.getStats().entrySet()) {
            stats.add(entry.getKey().getInternalName() + ": " + entry.getValue());
        }

        final StringJoiner achievements = new StringJoiner("\n");
        for (final AbstractAchievement achievement : userDb.getAchievements()) {
            achievements.add(achievement.getAchievementName());
        }

        final String primaryRank = userDb.getPrimaryRank().getRankName();
        final StringJoiner subRanks = new StringJoiner("; ");
        for (final Rank rank : userDb.getRanks()) {
            subRanks.add(rank.getRankName());
        }

        final StringJoiner permissions = new StringJoiner("\n");
        for (final int permissionId : userDb.getPermissionIds()) {
            this.getPermissionsModule().getPermissionFromId(permissionId).ifPresent(permissions::add);
        }

        final StringJoiner allPermissions = new StringJoiner("\n");
        for (final int permissionId : userDb.getAllPermissionIds()) {
            this.getPermissionsModule().getPermissionFromId(permissionId).ifPresent(allPermissions::add);
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("User Info")
                        .addField("Ranks", primaryRank + "[" + subRanks + "]", true)
                        .addField("Achievements", achievements.toString(), false)
                        .addField("Settings", settings.toString(), false)
                        .addField("Stats", stats.toString(), false)
                        .addField("User Perms", permissions.toString(), false)
                        .addField("All Perms", allPermissions.toString(), false)
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult unBanCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        if (!userDb.isBanned()) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle(ERROR_TITLE)
                            .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is not banned.")
            );
            return BaseCommandResult.SUCCESSFUL;
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Banned")
                        .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is now unBanned.")
        );
        userDb.setBanned(false);

        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult banCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        if (userDb.isBanned()) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle(ERROR_TITLE)
                            .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is already banned.")
            );
            return BaseCommandResult.SUCCESSFUL;
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Banned")
                        .setDescription(MarkdownUtil.monospace(discordUser.getAsTag()) + " is now banned.")
        );
        userDb.setBanned(true);

        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult setPrimaryRankCommand(final CommandParameters commandParameters, final UserDb userDb) {
        this.checkArgLength(commandParameters, 3);

        final Rank rank = ArgumentUtilities.getRankOrThrow(commandParameters, 2, this.rankModule);
        if (userDb.hasPrimaryRank(rank)) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle(ERROR_TITLE)
                            .setDescription("The user already has this rank.")
            );
            return BaseCommandResult.FAIL;
        }

        userDb.setPrimaryRank(rank);
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Set Primary Rank")
                        .setDescription(MarkdownUtil.monospace(rank.getRankName()))
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult rankCommand(final CommandParameters commandParameters, final UserDb userDb) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = ArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                this.getClass(),
                2,
                AddRemoveArgs.class
        );
        final Rank rank = ArgumentUtilities.getRankOrThrow(commandParameters, 3, this.rankModule);

        if (AddRemoveArgs.ADD == mode) {
            if (userDb.hasRank(rank)) {
                commandParameters.sendMessage(
                        commandParameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription("The user already has this rank.")
                );
                return BaseCommandResult.FAIL;
            }

            userDb.addRank(rank);
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Added Rank")
                            .setDescription(
                                    "Added %s rank to the user.",
                                    MarkdownUtil.monospace(rank.getRankName())
                            )
            );

        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!userDb.hasRank(rank)) {
                commandParameters.sendMessage(
                        commandParameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription("The user is not in possession of this rank.")
                );
                return BaseCommandResult.FAIL;
            }

            userDb.removeRank(rank);
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Removed Rank")
                            .setDescription(
                                    "Removed %s rank from the user.",
                                    MarkdownUtil.monospace(rank.getRankName())
                            )
            );
        }

        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult permsCommand(final CommandParameters commandParameters, final UserDb userDb, final User discordUser) {
        this.checkArgLength(commandParameters, 4);

        final AddRemoveArgs mode = ArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                this.getClass(),
                2,
                AddRemoveArgs.class
        );
        final int permissionId = this.getPermissionIdOrThrow(commandParameters, 3);
        final String permissionNode = this.getPermissionsModule().getPermissionFromId(permissionId)
                .orElseThrow(RuntimeException::new);

        if (AddRemoveArgs.ADD == mode) {
            if (userDb.hasPermission(permissionId)) {
                commandParameters.sendMessage(
                        commandParameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s does already possess the %s permission.",
                                        MarkdownUtil.monospace(discordUser.getAsTag()),
                                        MarkdownUtil.monospace(permissionNode)
                                )
                );
                return BaseCommandResult.FAIL;
            }

            userDb.addPermission(permissionId);
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Added Permission")
                            .setDescription(
                                    "%s added to %s.",
                                    MarkdownUtil.monospace(permissionNode),
                                    MarkdownUtil.monospace(discordUser.getAsTag())
                            )
            );
        } else if (AddRemoveArgs.REMOVE == mode) {
            if (!userDb.hasPermission(permissionId)) {
                commandParameters.sendMessage(
                        commandParameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription(
                                        "%s does not possess the %s permission.",
                                        MarkdownUtil.monospace(discordUser.getAsTag()),
                                        MarkdownUtil.monospace(permissionNode)
                                )
                );
                return BaseCommandResult.FAIL;
            }

            userDb.removePermission(permissionId);
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Removed Permission")
                            .setDescription(
                                    "%s removed from %s.",
                                    MarkdownUtil.monospace(permissionNode),
                                    MarkdownUtil.monospace(discordUser.getAsTag())
                            )
            );
        }

        return BaseCommandResult.SUCCESSFUL;
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
