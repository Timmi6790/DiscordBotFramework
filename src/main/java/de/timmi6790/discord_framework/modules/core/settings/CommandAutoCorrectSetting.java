package de.timmi6790.discord_framework.modules.core.settings;

import de.timmi6790.discord_framework.modules.setting.settings.BooleanSetting;

public class CommandAutoCorrectSetting extends BooleanSetting {
    public CommandAutoCorrectSetting() {
        super("core.settings.autocorrect", "AutoCorrection", "false");
    }
}
