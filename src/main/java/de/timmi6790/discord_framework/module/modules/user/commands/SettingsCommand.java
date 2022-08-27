package de.timmi6790.discord_framework.module.modules.user.commands;

import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.StringOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SettingsCommand extends SlashCommand {
    private static final Option<String> NEW_VALUE_OPTION = new StringOption("new_value", "New Value");

    private final SettingModule settingModule;

    private final Option<AbstractSetting<?>> settingOption;

    public SettingsCommand(final SettingModule settingModule,
                           final SlashCommandModule commandModule) {
        super("settings", "Change your settings");

        this.addProperties(
                new CategoryProperty("Info"),
                new SyntaxProperty("[setting] [newValue]"),
                new AliasNamesProperty("st", "setting")
        );

        this.settingModule = settingModule;

        this.settingOption = new SettingOption(settingModule);

        this.addOptions(
                this.settingOption,
                NEW_VALUE_OPTION
        );
    }

    private Optional<AbstractSetting<?>> getSettingThrow(final SlashCommandParameters commandParameters) {
        final Optional<AbstractSetting<?>> settingOpt = commandParameters.getOption(this.settingOption);
        if (settingOpt.isPresent()) {
            return settingOpt;
        }

        final Optional<String> settingNameOpt = commandParameters.getOptionAsString(this.settingOption);
        if (settingNameOpt.isEmpty()) {
            return Optional.empty();
        }

        final List<AbstractSetting<?>> similarSettings = DataUtilities.getSimilarityList(
                settingNameOpt.get(),
                commandParameters.getUserDb().getSettings().keySet(),
                AbstractSetting::getName,
                0.6,
                3
        );
        if (!similarSettings.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return Optional.ofNullable(similarSettings.get(0));
        }

        /*
        this.sendArgumentCorrectionMessage(
                commandParameters,
                settingName,
                argPos,
                "setting",
                this.getClass(),
                new String[0],
                similarSettings,
                AbstractSetting::getName
        );
         */
        throw new CommandReturnException();
    }

    @Override
    protected CommandResult onCommand(final SlashCommandParameters commandParameters) {
        final Optional<AbstractSetting<?>> settingOpt = this.getSettingThrow(commandParameters);

        // All current settings
        if (settingOpt.isEmpty()) {
            return this.showCurrentSettings(commandParameters);
        }

        final AbstractSetting<?> setting = settingOpt.get();
        final Optional<String> newValueOpt = commandParameters.getOption(NEW_VALUE_OPTION);

        // setting info
        if (newValueOpt.isEmpty()) {
            return this.showSettingInfo(commandParameters, setting);
        }

        // Change value
        return this.changeSetting(commandParameters, setting, newValueOpt.get());
    }

    private CommandResult showCurrentSettings(final SlashCommandParameters commandParameters) {
        final MultiEmbedBuilder embedBuilder = commandParameters.getEmbedBuilder()
                .setTitle("Settings");

        final Map<AbstractSetting<?>, String> playerSettings = commandParameters.getUserDb().getSettings();
        if (playerSettings.isEmpty()) {
            embedBuilder.setDescription("You can unlock settings by using the bot.");
        } else {
            for (final Map.Entry<AbstractSetting<?>, String> entry : playerSettings.entrySet()) {
                final String value = String.valueOf(entry.getValue());

                embedBuilder.addField(
                        entry.getKey().getName(),
                        String.format(
                                "%s%nValue: %s",
                                entry.getKey().getDescription(),
                                MarkdownUtil.monospace(value.isEmpty() ? EmbedBuilder.ZERO_WIDTH_SPACE : value)
                        ),
                        false
                );
            }
            embedBuilder.setFooterFormat(
                    "Tip: You can change the setting with /%s <statName> <newValue>",
                    this.getName()
            );
        }

        commandParameters.sendMessage(embedBuilder);
        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult showSettingInfo(final SlashCommandParameters commandParameters, final AbstractSetting<?> setting) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Setting - " + setting.getName())
                        .addField("Description", setting.getDescription())
                        .addField("Alias names", String.join(", ", setting.getAliasNames()))
                        .addField("Default value", String.valueOf(setting.getDefaultValue()))
                        .setFooterFormat(
                                "Tip: You can change the setting with /%s %s <newValue>",
                                this.getName(),
                                setting.getName()
                        )
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult changeSetting(final SlashCommandParameters commandParameters,
                                        final AbstractSetting<?> setting,
                                        final String newValue) {
        setting.handleCommand(commandParameters, newValue);
        return BaseCommandResult.SUCCESSFUL;
    }

    private static class SettingOption extends Option<AbstractSetting<?>> {
        private final SettingModule module;

        private SettingOption(final SettingModule module) {
            super("setting", "Setting", OptionType.STRING);

            this.module = module;

            this.addTypeOptions(module.getSettings().values());
        }

        @Override
        public String convertToOption(final AbstractSetting<?> option) {
            return option.getName();
        }

        @Override
        public Optional<AbstractSetting<?>> convertValue(final OptionMapping mapping) {
            return this.module.getSetting(mapping.getAsString());
        }
    }
}
