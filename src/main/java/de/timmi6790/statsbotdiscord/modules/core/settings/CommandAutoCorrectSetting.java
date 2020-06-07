package de.timmi6790.statsbotdiscord.modules.core.settings;

import de.timmi6790.statsbotdiscord.modules.setting.settings.BooleanSetting;

public class CommandAutoCorrectSetting extends BooleanSetting {
    public final static String INTERNAL_NAME = "core.settings.autocorrect";

    public CommandAutoCorrectSetting() {
        super(INTERNAL_NAME, "AutoCorrection", "false");
    }
}
