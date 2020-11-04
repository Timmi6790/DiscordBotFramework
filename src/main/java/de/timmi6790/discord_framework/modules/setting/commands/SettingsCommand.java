package de.timmi6790.discord_framework.modules.setting.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.modules.setting.SettingModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

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

        final List<AbstractSetting<?>> similarSettings = DataUtilities.getSimilarityList(
                settingName,
                commandParameters.getUserDb().getSettings().keySet(),
                AbstractSetting::getName,
                0.6,
                3
        );
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

        this.checkArgLength(commandParameters, 2);
        final AbstractSetting<?> setting = this.getSettingThrow(commandParameters, 0);

        // Change value
        final String raw = commandParameters.getRawArgs();
        final int firstSpace = raw.indexOf(" ");
        return this.changeSetting(commandParameters, setting, raw.substring(Math.min(firstSpace + 1, raw.length())));
    }

    private CommandResult showCurrentSettings(final CommandParameters commandParameters) {
        final MultiEmbedBuilder embedBuilder = this.getEmbedBuilder(commandParameters)
                .setTitle("Settings");

        final Map<AbstractSetting<?>, String> playerSettings = commandParameters.getUserDb().getSettings();
        if (playerSettings.isEmpty()) {
            embedBuilder.setDescription("You can unlock settings by using the bot.");
        } else {
            for (final Map.Entry<AbstractSetting<?>, String> entry : playerSettings.entrySet()) {
                embedBuilder.addField(
                        entry.getKey().getName(),
                        String.format(
                                "%s%nValue: %s",
                                entry.getKey().getDescription(),
                                MarkdownUtil.monospace(String.valueOf(entry.getValue()))
                        ),
                        false
                );
            }
            embedBuilder.setFooterFormat(
                    "Tip: You can change the setting with %s%s <statName> <newValue>",
                    getCommandModule().getMainCommand(),
                    this.getName()
            );
        }

        this.sendTimedMessage(
                commandParameters,
                embedBuilder,
                400
        );
        return CommandResult.SUCCESS;
    }

    private CommandResult changeSetting(final CommandParameters commandParameters,
                                        final AbstractSetting<?> setting,
                                        final String newValue) {
        setting.handleCommand(commandParameters, newValue);
        return CommandResult.SUCCESS;
    }
}
