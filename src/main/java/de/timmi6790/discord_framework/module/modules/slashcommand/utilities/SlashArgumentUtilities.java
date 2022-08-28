package de.timmi6790.discord_framework.module.modules.slashcommand.utilities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SlashArgumentUtilities {
/*
    public <T> T getOrThrow(final SlashCommandParameters commandParameters,
                            final int argPosition,
                            final Function<String, Optional<T>> convertFunction,
                            final String argumentType) {
        final String arg = commandParameters.getArg(argPosition);
        final Optional<T> parsedOpt = convertFunction.apply(arg);
        if (parsedOpt.isPresent()) {
            return parsedOpt.get();
        }

        SlashMessageUtilities.sendInvalidArgumentMessage(
                commandParameters,
                arg,
                argumentType
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

 */
}
