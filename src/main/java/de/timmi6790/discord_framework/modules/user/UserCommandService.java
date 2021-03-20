package de.timmi6790.discord_framework.modules.user;

import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.OptionalDependency;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.modules.user.commands.SettingsCommand;
import de.timmi6790.discord_framework.modules.user.commands.UserCommand;

// TODO: FIX ME
// @Service
public class UserCommandService {
    public UserCommandService(final UserDbModule userDbModule,
                              final CommandModule commandModule,
                              @OptionalDependency final SettingModule settingModule) {
        commandModule.registerCommands(
                userDbModule,
                new UserCommand()
        );

        if (settingModule != null) {
            commandModule.registerCommands(
                    userDbModule,
                    new SettingsCommand()
            );
        }
    }
}
