package de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info;

import de.timmi6790.discord_framework.module.modules.slashcommand.property.SlashCommandProperty;

public class AliasNamesProperty implements SlashCommandProperty<String[]> {
    private final String[] aliasNames;

    public AliasNamesProperty(final String... aliasNames) {
        this.aliasNames = aliasNames.clone();
    }

    @Override
    public String[] getValue() {
        return this.aliasNames.clone();
    }
}
