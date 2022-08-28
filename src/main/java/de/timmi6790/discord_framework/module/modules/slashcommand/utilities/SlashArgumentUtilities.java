package de.timmi6790.discord_framework.module.modules.slashcommand.utilities;

import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@UtilityClass
public class SlashArgumentUtilities {
    public int getPermissionIdOrThrow(final SlashCommandParameters commandParameters,
                                      final Option<String> option,
                                      final SlashCommandModule commandModule,
                                      @Nullable final SettingModule settingsModule,
                                      final PermissionsModule permissionModule) {
        final String arg = commandParameters.getOptionOrThrow(option);

        // Check for command names
        final Optional<SlashCommand> commandOpt = commandModule.getCommand(arg);
        if (commandOpt.isPresent()) {
            final SlashCommand command = commandOpt.get();
            if (command.hasDefaultPermission()) {
                SlashMessageUtilities.sendErrorMessage(
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

        SlashMessageUtilities.sendInvalidArgumentMessage(
                commandParameters,
                arg,
                "permission"
        );
        throw new CommandReturnException();
    }
}
