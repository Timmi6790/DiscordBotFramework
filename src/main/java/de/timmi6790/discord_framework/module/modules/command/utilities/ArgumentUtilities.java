package de.timmi6790.discord_framework.module.modules.command.utilities;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.discord_framework.utilities.commons.EnumUtilities;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ArgumentUtilities {
    private static final Pattern DISCORD_USER_ID_PATTERN = Pattern.compile("^(<@[!&])?(\\d*)>?$");

    private static final Pattern ARGUMENT_SPLITTER_PATTERN = Pattern.compile("\\s+");

    public String[] parseRawArguments(String rawArgument) {
        rawArgument = rawArgument.trim();
        if (rawArgument.isEmpty()) {
            return new String[0];
        }

        return ARGUMENT_SPLITTER_PATTERN.split(rawArgument);
    }

    public <E extends Enum<?>> E getFromEnumIgnoreCaseOrThrow(@NonNull final CommandParameters commandParameters,
                                                              final int argPos,
                                                              @NonNull final E[] enumValue) {
        final String userArg = commandParameters.getArg(argPos);
        final Optional<E> arg = EnumUtilities.getIgnoreCase(userArg, enumValue);
        if (arg.isPresent()) {
            return arg.get();
        }

        // TODO: Add help menu
        /*
        this.sendHelpMessage(commandParameters,
                userArg,
                argPos,
                "argument",
                null,
                null,
                EnumUtilities.getPrettyNames(enumValue)
        );

         */
        throw new CommandReturnException();
    }

    public Optional<Command> getCommand(final CommandParameters commandParameters,
                                        final CommandModule commandModule,
                                        final String commandName) {
        final Optional<Command> commandOpt = commandModule.getCommand(commandName);
        if (commandOpt.isPresent()) {
            return commandOpt;
        }

        final List<Command> similarCommands = DataUtilities.getSimilarityList(
                commandName,
                commandModule.getCommands(command -> command.canExecute(commandParameters)),
                Command::getName,
                0.6,
                5
        );
        if (similarCommands.isEmpty()) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Can't find a valid command")
                            .setDescription(
                                    "Your input %s is not similar with one of the valid commands." +
                                            "Use the %s command to see all valid commands.",
                                    MarkdownUtil.monospace(commandName),
                                    MarkdownUtil.monospace(commandModule.getMainCommand() + "help")
                            )
            );

            return Optional.empty();
        }

        // Handle auto correction
        if (commandParameters.getUserDb().hasAutoCorrection()) {
            return Optional.of(similarCommands.get(0));
        }

        // TODO: Send help thing
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Can't find a valid command")
                        .setDescription("TODO: Shown help menu")
        );

        return Optional.empty();
    }

    public User getDiscordUserOrThrow(final CommandParameters commandParameters,
                                      final int argPos) {
        final String discordUserName = commandParameters.getArg(argPos);
        final Matcher userIdMatcher = DISCORD_USER_ID_PATTERN.matcher(discordUserName);
        if (userIdMatcher.find()) {
            final User user = commandParameters.getUserDb()
                    .getUserDbModule()
                    .getDiscordUserCache()
                    .get(Long.valueOf(userIdMatcher.group(2)));
            if (user != null) {
                return user;
            }
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid User")
                        .setDescription(MarkdownUtil.monospace(discordUserName) + " is not a valid discord user.")
        );
        throw new CommandReturnException();
    }

    public int getPermissionIdOrThrow(final CommandParameters commandParameters,
                                      final int argPos,
                                      final CommandModule commandModule,
                                      @Nullable final SettingModule settingsModule,
                                      final PermissionsModule permissionModule) {
        final String permArg = commandParameters.getArg(argPos);

        // Check for command names
        final Optional<Command> commandOpt = commandModule.getCommand(permArg);
        if (commandOpt.isPresent()) {
            final Command command = commandOpt.get();
            if (command.hasDefaultPermission()) {
                commandParameters.sendMessage(
                        commandParameters.getEmbedBuilder()
                                .setTitle("Error")
                                .setDescription(MarkdownUtil.monospace(command.getName()) + " command has no permission.")
                );
                throw new CommandReturnException();
            }

            return command.getPermissionId();
        }

        // check for setting names
        if (settingsModule != null) {
            final Optional<AbstractSetting<?>> settingOpt = settingsModule.getSetting(permArg);
            if (settingOpt.isPresent()) {
                return settingOpt.get().getPermissionId();
            }
        }

        final Optional<Integer> permissionIdOpt = permissionModule
                .getPermissionId(permArg);

        if (permissionIdOpt.isPresent()) {
            return permissionIdOpt.get();
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Error")
                        .setDescription(MarkdownUtil.monospace(permArg) + " is not a valid permission.")
        );
        throw new CommandReturnException();
    }

    public Rank getRankOrThrow(final CommandParameters commandParameters,
                               final int position,
                               final RankModule rankModule) {
        final String userInput = commandParameters.getArg(position);
        final Optional<Rank> rankOpt = rankModule.getRank(userInput);
        if (rankOpt.isPresent()) {
            return rankOpt.get();
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Error")
                        .setDescription(MarkdownUtil.monospace(userInput) + " is not a valid rank.")
        );
        throw new CommandReturnException();
    }
}
