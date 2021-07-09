package de.timmi6790.discord_framework.module.modules.command.property.properties.info;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SyntaxPropertyTest {
    @ParameterizedTest
    @ValueSource(strings = {"<Test>", "<Test|DDDD> [DDDD]", ""})
    void getValue(final String syntax) {
        final SyntaxProperty property = new SyntaxProperty(syntax);
        assertThat(property.getValue()).isEqualTo(syntax);
    }
}