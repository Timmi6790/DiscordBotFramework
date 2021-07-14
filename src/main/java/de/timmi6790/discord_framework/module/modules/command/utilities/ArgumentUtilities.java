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
import de.timmi6790.discord_framework.utilities.commons.EnumUtilities;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ArgumentUtilities {
    private static final Pattern DISCORD_USER_ID_PATTERN = Pattern.compile("^(<@[!&])?(\\d+)>?$");

    private static final Pattern ARGUMENT_SPLITTER_PATTERN = Pattern.compile("\\s+");

    private <T extends Number> T getNumberOrThrow(final CommandParameters commandParameters,
                                                  final int argPosition,
                                                  final Function<String, T> convertFunction,
                                                  final String argumentType) {
        return getOrThrow(
                commandParameters,
                argPosition,
                arg -> {
                    try {
                        return Optional.of(convertFunction.apply(arg));
                    } catch (final NumberFormatException e) {
                        return Optional.empty();
                    }
                },
                argumentType
        );
    }

    public String[] parseRawArguments(String rawArgument) {
        rawArgument = rawArgument.trim();
        if (rawArgument.isEmpty()) {
            return new String[0];
        }

        return ARGUMENT_SPLITTER_PATTERN.split(rawArgument);
    }

    public <E extends Enum<?>> E getFromEnumIgnoreCaseOrThrow(final CommandParameters commandParameters,
                                                              final Class<? extends Command> commandClass,
                                                              final int argPos,
                                                              final Class<E> enumClass) {
        return getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                commandClass,
                argPos,
                enumClass.getEnumConstants()
        );
    }

    public <E extends Enum<?>> E getFromEnumIgnoreCaseOrThrow(final CommandParameters commandParameters,
                                                              final Class<? extends Command> commandClass,
                                                              final int argPos,
                                                              final E[] enumValues) {
        final String userArg = commandParameters.getArg(argPos);
        final Optional<E> arg = EnumUtilities.getIgnoreCase(userArg, enumValues);
        if (arg.isPresent()) {
            return arg.get();
        }

        commandParameters.getCommandModule().sendArgumentCorrectionMessage(
                commandParameters,
                userArg,
                argPos,
                "argument",
                null,
                new String[0],
                commandClass,
                Arrays.asList(enumValues),
                EnumUtilities::getPrettyName
        );

        throw new CommandReturnException();
    }

    public <T> T getOrThrow(final CommandParameters commandParameters,
                            final int argPosition,
                            final Function<String, Optional<T>> convertFunction,
                            final String argumentType) {
        final String arg = commandParameters.getArg(argPosition);
        final Optional<T> parsedOpt = convertFunction.apply(arg);
        if (parsedOpt.isPresent()) {
            return parsedOpt.get();
        }

        MessageUtilities.sendInvalidArgumentMessage(
                commandParameters,
                arg,
                argumentType
        );
        throw new CommandReturnException();
    }

    public User getDiscordUserOrThrow(final CommandParameters commandParameters,
                                      final int argPos) {
        final String input = commandParameters.getArg(argPos);
        final Matcher userIdMatcher = DISCORD_USER_ID_PATTERN.matcher(input);
        if (userIdMatcher.find()) {
            // The type is guaranteed through the regex
            final long userId = Long.parseLong(userIdMatcher.group(2));
            final User user = commandParameters.getUserDb()
                    .getUserDbModule()
                    .getDiscordUserCache()
                    .get(userId);
            if (user != null) {
                return user;
            }
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid User")
                        .setDescription(MarkdownUtil.monospace(input) + " is not a valid discord user.")
        );
        throw new CommandReturnException();
    }

    public int getPermissionIdOrThrow(final CommandParameters commandParameters,
                                      final int argPos,
                                      final CommandModule commandModule,
                                      @Nullable final SettingModule settingsModule,
                                      final PermissionsModule permissionModule) {
        final String arg = commandParameters.getArg(argPos);

        // Check for command names
        final Optional<Command> commandOpt = commandModule.getCommand(arg);
        if (commandOpt.isPresent()) {
            final Command command = commandOpt.get();
            if (command.hasDefaultPermission()) {
                MessageUtilities.sendErrorMessage(
                        commandParameters,
                        MarkdownUtil.monospace(command.getName()) + " command has no permission."
                );
                throw new CommandReturnException();
            }

            return command.getPermissionId();
        }

        // check for setting names
        if (settingsModule != null) {
            final Optional<AbstractSetting<?>> settingOpt = settingsModule.getSetting(arg);
            if (settingOpt.isPresent()) {
                return settingOpt.get().getPermissionId();
            }
        }

        final Optional<Integer> permissionIdOpt = permissionModule
                .getPermissionId(arg);
        if (permissionIdOpt.isPresent()) {
            return permissionIdOpt.get();
        }

        MessageUtilities.sendInvalidArgumentMessage(
                commandParameters,
                arg,
                "permission"
        );
        throw new CommandReturnException();
    }

    public Rank getRankOrThrow(final CommandParameters commandParameters,
                               final int argPosition,
                               final RankModule rankModule) {
        return getOrThrow(
                commandParameters,
                argPosition,
                rankModule::getRank,
                "rank"
        );
    }

    public byte getByteOrThrow(final CommandParameters commandParameters, final int argPosition) {
        return getNumberOrThrow(
                commandParameters,
                argPosition,
                Byte::parseByte,
                "byte"
        );
    }

    public short getShortOrThrow(final CommandParameters commandParameters, final int argPosition) {
        return getNumberOrThrow(
                commandParameters,
                argPosition,
                Short::parseShort,
                "short"
        );
    }

    public int getIntOrThrow(final CommandParameters commandParameters, final int argPosition) {
        return getNumberOrThrow(
                commandParameters,
                argPosition,
                Integer::parseInt,
                "integer"
        );
    }

    public long getLongOrThrow(final CommandParameters commandParameters, final int argPosition) {
        return getNumberOrThrow(
                commandParameters,
                argPosition,
                Long::parseLong,
                "long"
        );
    }

    public float getFloatOrThrow(final CommandParameters commandParameters, final int argPosition) {
        return getNumberOrThrow(
                commandParameters,
                argPosition,
                Float::parseFloat,
                "float"
        );
    }

    public double getDoubleOrThrow(final CommandParameters commandParameters, final int argPosition) {
        return getNumberOrThrow(
                commandParameters,
                argPosition,
                Double::parseDouble,
                "double"
        );
    }
}
