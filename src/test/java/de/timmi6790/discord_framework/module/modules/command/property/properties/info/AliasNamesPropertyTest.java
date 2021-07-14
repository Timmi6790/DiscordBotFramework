package de.timmi6790.discord_framework.module.modules.command.property.properties.info;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AliasNamesPropertyTest {
    @Test
    void getValue() {
        final String[] aliasNames = new String[]{
                "one",
                "two",
                "three"
        };

        final AliasNamesProperty property = new AliasNamesProperty(aliasNames);
        assertThat(property.getValue()).isEqualTo(aliasNames);
    }
}