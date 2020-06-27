package de.timmi6790.statsbotdiscord.modules.core.settings;

import de.timmi6790.statsbotdiscord.modules.setting.settings.BooleanSetting;

public class CommandAutoCorrectSetting extends BooleanSetting {
    public CommandAutoCorrectSetting() {
        super("core.settings.autocorrect", "AutoCorrection", "false");
    }
}
