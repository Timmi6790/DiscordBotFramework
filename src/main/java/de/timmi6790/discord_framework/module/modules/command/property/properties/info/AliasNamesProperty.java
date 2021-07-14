package de.timmi6790.discord_framework.module.modules.command.property.properties.info;

import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;

public class AliasNamesProperty implements CommandProperty<String[]> {
    private final String[] aliasNames;

    public AliasNamesProperty(final String... aliasNames) {
        this.aliasNames = aliasNames.clone();
    }

    @Override
    public String[] getValue() {
        return this.aliasNames.clone();
    }
}
