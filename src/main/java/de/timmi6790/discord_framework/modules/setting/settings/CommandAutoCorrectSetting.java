package de.timmi6790.discord_framework.modules.setting.settings;

public class CommandAutoCorrectSetting extends BooleanSetting {
    public CommandAutoCorrectSetting() {
        super(
                "AutoCorrection",
                "Selects the first suggested correction on invalid input.",
                false,
                "ac"
        );
    }
}
