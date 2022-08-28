package de.timmi6790.discord_framework.module.modules.user.commands;

import de.timmi6790.discord_framework.module.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.rank.options.RankOption;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandGroup;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.DiscordUserOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.EnumOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.StringOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.utilities.SlashArgumentUtilities;
import de.timmi6790.discord_framework.module.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Map;
import java.util.StringJoiner;

import static de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult.FAIL;

public class UserCommand extends SlashCommandGroup {
    private static final String ERROR_TITLE = "Error";

    private static final Option<User> DISCORD_USER_OPTION_REQUIRED = new DiscordUserOption("user", "Targeted user").setRequired(true);

    private final UserDbModule userDbModule;

    public UserCommand(final SlashCommandModule slashCommandModule, final UserDbModule userDbModule, final PermissionsModule permissionsModule,
                       final RankModule rankModule, final SettingModule settingModule) {
        super(slashCommandModule, "user", "User control command");

        this.userDbModule = userDbModule;

        this.addSubcommands(
                new InfoCommand(slashCommandModule, permissionsModule),
                new InvalidateCommand(slashCommandModule),
                new SetPrimaryRankCommand(slashCommandModule, rankModule),
                new UnBanCommand(slashCommandModule),
                new BanCommand(slashCommandModule),
                new RankCommand(slashCommandModule, rankModule),
                new PermsCommand(slashCommandModule, permissionsModule, settingModule)
        );
    }

    private UserDb getUserDb(final SlashCommandParameters parameters) {
        final User user = parameters.getOptionOrThrow(DISCORD_USER_OPTION_REQUIRED);
        return this.userDbModule.getOrCreate(user.getIdLong());
    }

    private class InfoCommand extends SlashCommand {
        private final PermissionsModule permissionsModule;

        public InfoCommand(final SlashCommandModule slashCommandModule, final PermissionsModule permissionsModule) {
            super(slashCommandModule, "info", "Info");

            this.permissionsModule = permissionsModule;

            this.addOptions(
                    DISCORD_USER_OPTION_REQUIRED
            );
        }

        @Override
        protected CommandResult onCommand(final SlashCommandParameters parameters) {
            final UserDb userDb = UserCommand.this.getUserDb(parameters);

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
                this.permissionsModule.getPermissionFromId(permissionId).ifPresent(permissions::add);
            }

            final StringJoiner allPermissions = new StringJoiner("\n");
            for (final int permissionId : userDb.getAllPermissionIds()) {
                this.permissionsModule.getPermissionFromId(permissionId).ifPresent(allPermissions::add);
            }

            parameters.sendMessage(
                    parameters.getEmbedBuilder()
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
    }

    private class InvalidateCommand extends SlashCommand {
        public InvalidateCommand(final SlashCommandModule slashCommandModule) {
            super(slashCommandModule, "invalidate", "Invalidate cache");

            this.addOptions(
                    DISCORD_USER_OPTION_REQUIRED
            );
        }

        @Override
        protected CommandResult onCommand(final SlashCommandParameters parameters) {
            final UserDb userDb = UserCommand.this.getUserDb(parameters);
            UserCommand.this.userDbModule.invalidateCache(userDb.getDiscordId());

            parameters.sendMessage(
                    parameters.getEmbedBuilder()
                            .setTitle("Invalidated Cache")
                            .setDescription(MarkdownUtil.monospace(String.valueOf(userDb.getDiscordId())) + " was removed from the cache.")
            );

            return BaseCommandResult.SUCCESSFUL;
        }
    }

    private class SetPrimaryRankCommand extends SlashCommand {
        private final Option<Rank> primaryRankOption;

        public SetPrimaryRankCommand(final SlashCommandModule slashCommandModule, final RankModule rankModule) {
            super(slashCommandModule, "primary_rank", "Set Primary Rank");

            this.primaryRankOption = new RankOption("rank", "New Primary Rank", rankModule).setRequired(true);

            this.addOptions(
                    DISCORD_USER_OPTION_REQUIRED,
                    this.primaryRankOption
            );
        }

        @Override
        protected CommandResult onCommand(final SlashCommandParameters parameters) {
            final UserDb userDb = UserCommand.this.getUserDb(parameters);
            final Rank rank = parameters.getOptionOrThrow(this.primaryRankOption);

            if (userDb.hasPrimaryRank(rank)) {
                parameters.sendMessage(
                        parameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription("The user already has this rank.")
                );
                return FAIL;
            }

            userDb.setPrimaryRank(rank);
            parameters.sendMessage(
                    parameters.getEmbedBuilder()
                            .setTitle("Set Primary Rank")
                            .setDescription(MarkdownUtil.monospace(rank.getRankName()))
            );

            return BaseCommandResult.SUCCESSFUL;
        }
    }

    private class UnBanCommand extends SlashCommand {
        public UnBanCommand(final SlashCommandModule slashCommandModule) {
            super(slashCommandModule, "unban", "Unban a player");

            this.addOptions(
                    DISCORD_USER_OPTION_REQUIRED
            );
        }

        @Override
        protected CommandResult onCommand(final SlashCommandParameters parameters) {
            final UserDb userDb = UserCommand.this.getUserDb(parameters);
            if (!userDb.isBanned()) {
                parameters.sendMessage(
                        parameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription(MarkdownUtil.monospace(userDb.getUser().getAsTag()) + " is not banned.")
                );
                return BaseCommandResult.SUCCESSFUL;
            }

            parameters.sendMessage(
                    parameters.getEmbedBuilder()
                            .setTitle("Banned")
                            .setDescription(MarkdownUtil.monospace(userDb.getUser().getAsTag()) + " is now unBanned.")
            );
            userDb.setBanned(false);

            return BaseCommandResult.SUCCESSFUL;
        }
    }

    private class BanCommand extends SlashCommand {
        public BanCommand(final SlashCommandModule slashCommandModule) {
            super(slashCommandModule, "ban", "Bans a player");

            this.addOptions(
                    DISCORD_USER_OPTION_REQUIRED
            );
        }

        @Override
        protected CommandResult onCommand(final SlashCommandParameters parameters) {
            final UserDb userDb = UserCommand.this.getUserDb(parameters);

            if (userDb.isBanned()) {
                parameters.sendMessage(
                        parameters.getEmbedBuilder()
                                .setTitle(ERROR_TITLE)
                                .setDescription(MarkdownUtil.monospace(userDb.getUser().getAsTag()) + " is already banned.")
                );
                return BaseCommandResult.SUCCESSFUL;
            }

            parameters.sendMessage(
                    parameters.getEmbedBuilder()
                            .setTitle("Banned")
                            .setDescription(MarkdownUtil.monospace(userDb.getUser().getAsTag()) + " is now banned.")
            );
            userDb.setBanned(true);

            return BaseCommandResult.SUCCESSFUL;
        }
    }

    private class RankCommand extends SlashCommand {
        private final Option<Rank> rankOption;
        private final Option<Arguments> argumentsOption;

        public RankCommand(final SlashCommandModule slashCommandModule, final RankModule rankModule) {
            super(slashCommandModule, "rank", "Rank Control");

            this.rankOption = new RankOption("rank", "Rank", rankModule).setRequired(true);
            this.argumentsOption = new EnumOption<>(Arguments.class, "mode", "Control mode").setRequired(true);

            this.addOptions(
                    DISCORD_USER_OPTION_REQUIRED,
                    this.argumentsOption,
                    this.rankOption
            );
        }

        @Override
        protected CommandResult onCommand(final SlashCommandParameters parameters) {
            final UserDb userDb = UserCommand.this.getUserDb(parameters);

            final Arguments mode = parameters.getOptionOrThrow(this.argumentsOption);
            final Rank rank = parameters.getOptionOrThrow(this.rankOption);

            if (Arguments.ADD == mode) {
                if (userDb.hasRank(rank)) {
                    parameters.sendMessage(
                            parameters.getEmbedBuilder()
                                    .setTitle(ERROR_TITLE)
                                    .setDescription("The user already has this rank.")
                    );
                    return FAIL;
                }

                userDb.addRank(rank);
                parameters.sendMessage(
                        parameters.getEmbedBuilder()
                                .setTitle("Added Rank")
                                .setDescription(
                                        "Added %s rank to the user.",
                                        MarkdownUtil.monospace(rank.getRankName())
                                )
                );

            } else if (Arguments.REMOVE == mode) {
                if (!userDb.hasRank(rank)) {
                    parameters.sendMessage(
                            parameters.getEmbedBuilder()
                                    .setTitle(ERROR_TITLE)
                                    .setDescription("The user is not in possession of this rank.")
                    );
                    return FAIL;
                }

                userDb.removeRank(rank);
                parameters.sendMessage(
                        parameters.getEmbedBuilder()
                                .setTitle("Removed Rank")
                                .setDescription(
                                        "Removed %s rank from the user.",
                                        MarkdownUtil.monospace(rank.getRankName())
                                )
                );
            }

            return BaseCommandResult.SUCCESSFUL;
        }

        public enum Arguments {
            ADD,
            REMOVE
        }
    }

    private class PermsCommand extends SlashCommand {
        private final PermissionsModule permissionsModule;
        public final SettingModule settingModule;

        private final Option<Arguments> argumentsOption;
        private final Option<String> permissionIdOption;

        public PermsCommand(final SlashCommandModule slashCommandModule, final PermissionsModule permissionsModule, final SettingModule settingModule) {
            super(slashCommandModule, "perms", "Permission Control");

            this.permissionsModule = permissionsModule;
            this.settingModule = settingModule;

            this.argumentsOption = new EnumOption<>(Arguments.class, "mode", "Control mode").setRequired(true);
            this.permissionIdOption = new StringOption("permission", "command|permNode").setRequired(true);

            this.addOptions(
                    DISCORD_USER_OPTION_REQUIRED,
                    this.argumentsOption,
                    this.permissionIdOption
            );
        }

        private int getPermissionIdOrThrow(final SlashCommandParameters commandParameters) {
            return SlashArgumentUtilities.getPermissionIdOrThrow(
                    commandParameters,
                    this.permissionIdOption,
                    this.getModule(),
                    this.settingModule,
                    this.permissionsModule
            );
        }

        @Override
        protected CommandResult onCommand(final SlashCommandParameters parameters) {
            final UserDb userDb = UserCommand.this.getUserDb(parameters);
            final Arguments mode = parameters.getOptionOrThrow(this.argumentsOption);
            final int permissionId = this.getPermissionIdOrThrow(parameters);
            final String permissionNode = this.permissionsModule.getPermissionFromId(permissionId)
                    .orElseThrow(RuntimeException::new);

            if (Arguments.ADD == mode) {
                if (userDb.hasPermission(permissionId)) {
                    parameters.sendMessage(
                            parameters.getEmbedBuilder()
                                    .setTitle(ERROR_TITLE)
                                    .setDescription(
                                            "%s does already possess the %s permission.",
                                            MarkdownUtil.monospace(userDb.getUser().getAsTag()),
                                            MarkdownUtil.monospace(permissionNode)
                                    )
                    );
                    return BaseCommandResult.FAIL;
                }

                userDb.addPermission(permissionId);
                parameters.sendMessage(
                        parameters.getEmbedBuilder()
                                .setTitle("Added Permission")
                                .setDescription(
                                        "%s added to %s.",
                                        MarkdownUtil.monospace(permissionNode),
                                        MarkdownUtil.monospace(userDb.getUser().getAsTag())
                                )
                );
            } else if (Arguments.REMOVE == mode) {
                if (!userDb.hasPermission(permissionId)) {
                    parameters.sendMessage(
                            parameters.getEmbedBuilder()
                                    .setTitle(ERROR_TITLE)
                                    .setDescription(
                                            "%s does not possess the %s permission.",
                                            MarkdownUtil.monospace(userDb.getUser().getAsTag()),
                                            MarkdownUtil.monospace(permissionNode)
                                    )
                    );
                    return BaseCommandResult.FAIL;
                }

                userDb.removePermission(permissionId);
                parameters.sendMessage(
                        parameters.getEmbedBuilder()
                                .setTitle("Removed Permission")
                                .setDescription(
                                        "%s removed from %s.",
                                        MarkdownUtil.monospace(permissionNode),
                                        MarkdownUtil.monospace(userDb.getUser().getAsTag())
                                )
                );
            }

            return BaseCommandResult.SUCCESSFUL;
        }

        public enum Arguments {
            ADD,
            REMOVE
        }
    }
}
