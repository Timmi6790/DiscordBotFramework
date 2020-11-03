package de.timmi6790.discord_framework.modules.setting.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SettingsCommand extends AbstractCommand {
    private final SettingModule settingModule;

    public SettingsCommand() {
        super("settings",
                "Info",
                "Settings",
                "[setting] [newValue]",
                "", "st");

        this.settingModule = getModuleManager().getModuleOrThrow(SettingModule.class);
    }

    private AbstractSetting<?> getSettingThrow(final CommandParameters commandParameters, final int argPos) {
        final String settingName = commandParameters.getArgs()[argPos];

        final Optional<AbstractSetting<?>> settingOpt = this.getSettingModule().getSetting(settingName);
        if (settingOpt.isPresent()) {
            return settingOpt.get();
        }

        final List<AbstractSetting<?>> similarSettings = new ArrayList<>();
        if (!similarSettings.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarSettings.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                settingName,
                argPos,
                "setting",
                this,
                new String[0],
                similarSettings.stream().map(AbstractSetting::getName).collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final int argsLength = commandParameters.getArgs().length;

        // All current settings
        if (argsLength == 0) {
            return this.showCurrentSettings(commandParameters);
        }

        final AbstractSetting<?> setting = this.getSettingThrow(commandParameters, 1);

        // Show setting info
        if (argsLength == 1) {
            return this.showSettingInfo(commandParameters, setting);
        }

        // Change value
        return this.changeSetting(commandParameters, setting, commandParameters.getRawArgs());
    }

    private CommandResult showCurrentSettings(final CommandParameters commandParameters) {
        final MultiEmbedBuilder embedBuilder = this.getEmbedBuilder(commandParameters)
                .setTitle("Settings");

        final Map<Integer, String> settingsMap = commandParameters.getUserDb().getSettings();
        if (settingsMap.isEmpty()) {
            embedBuilder.setDescription("You can unlock settings by using the bot.");
        } else {
            settingsMap.forEach((settings, value) -> embedBuilder.addField(String.valueOf(settings.intValue()), value, false));
        }

        this.sendTimedMessage(
                commandParameters,
                embedBuilder,
                90
        );
        return CommandResult.SUCCESS;
    }

    private CommandResult showSettingInfo(final CommandParameters commandParameters,
                                          final AbstractSetting<?> setting) {

        return CommandResult.SUCCESS;
    }

    private CommandResult changeSetting(final CommandParameters commandParameters,
                                        final AbstractSetting<?> setting,
                                        final String newValue) {

        return CommandResult.SUCCESS;
    }
}
