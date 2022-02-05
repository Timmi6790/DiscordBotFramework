package de.timmi6790.discord_framework.module.modules.command.property.properties.info;

import net.dv8tion.jda.api.Permission;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequiredDiscordBotPermsPropertyTest {
    @Test
    void getValue() {
        final Permission[] permissions = new Permission[]{Permission.MESSAGE_SEND, Permission.ADMINISTRATOR};
        final RequiredDiscordBotPermsProperty property = new RequiredDiscordBotPermsProperty(permissions);
        assertThat(property.getValue()).containsExactlyInAnyOrder(permissions);
    }
}