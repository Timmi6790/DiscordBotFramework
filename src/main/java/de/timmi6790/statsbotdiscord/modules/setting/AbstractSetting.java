package de.timmi6790.statsbotdiscord.modules.setting;

import lombok.Data;

@Data
public abstract class AbstractSetting<T> {
    private int dbId = 0;
    private final String internalName;
    private final String name;
    private final boolean requirePerms;
    private final String permNode;
    private final String defaultValues;

    public abstract T parseSetting(String setting);
}
