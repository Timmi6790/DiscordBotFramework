package de.timmi6790.discord_framework.module.modules.user.commands;

import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SettingsCommand extends AbstractCommand {
    private final SettingModule settingModule;

    public SettingsCommand() {
        super("settings",
                "Info",
                "Settings",
                "[setting] [newValue]",
                "st", "setting");

        this.settingModule = this.getModuleManager().getModuleOrThrow(SettingModule.class);
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
                AbstractSetting::getStatName,
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
                this.getClass(),
                new String[0],
                DataUtilities.convertToStringList(similarSettings, AbstractSetting::getStatName)
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

        final AbstractSetting<?> setting = this.getSettingThrow(commandParameters, 0);

        // setting info
        if (argsLength == 1) {
            return this.showSettingInfo(commandParameters, setting);
        }

        // Change value
        final String raw = commandParameters.getRawArgs();
        final int firstSpace = raw.indexOf(' ');
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
                final String value = String.valueOf(entry.getValue());

                embedBuilder.addField(
                        entry.getKey().getStatName(),
                        String.format(
                                "%s%nValue: %s",
                                entry.getKey().getDescription(),
                                MarkdownUtil.monospace(value.isEmpty() ? EmbedBuilder.ZERO_WIDTH_SPACE : value)
                        ),
                        false
                );
            }
            embedBuilder.setFooterFormat(
                    "Tip: You can change the setting with %s%s <statName> <newValue>",
                    this.getCommandModule().getMainCommand(),
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

    private CommandResult showSettingInfo(final CommandParameters commandParameters, final AbstractSetting<?> setting) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Setting - " + setting.getStatName())
                        .addField("Description", setting.getDescription())
                        .addField("Alias names", String.join(", ", setting.getAliasNames()))
                        .addField("Default value", String.valueOf(setting.getDefaultValue()))
                        .setFooterFormat(
                                "Tip: You can change the setting with %s%s %s <newValue>",
                                this.getCommandModule().getMainCommand(),
                                this.getName(),
                                setting.getStatName()
                        ),
                300
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
