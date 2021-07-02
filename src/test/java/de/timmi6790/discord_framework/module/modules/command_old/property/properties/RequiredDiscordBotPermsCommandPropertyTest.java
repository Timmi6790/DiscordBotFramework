package de.timmi6790.discord_framework.module.modules.command_old.property.properties;

import net.dv8tion.jda.api.Permission;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class RequiredDiscordBotPermsCommandPropertyTest {
    @Test
    void getValueEmpty() {
        final RequiredDiscordBotPermsCommandProperty property = new RequiredDiscordBotPermsCommandProperty();
        assertThat(property.getValue()).isEmpty();
    }

    @Test
    void getValue() {
        final Permission[] input = {Permission.MESSAGE_MANAGE, Permission.MESSAGE_MENTION_EVERYONE};
        final RequiredDiscordBotPermsCommandProperty property = new RequiredDiscordBotPermsCommandProperty(input);
        assertThat(property.getValue()).containsExactly(input);
    }
}