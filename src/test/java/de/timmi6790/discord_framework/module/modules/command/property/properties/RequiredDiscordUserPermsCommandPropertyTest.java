package de.timmi6790.discord_framework.module.modules.command.property.properties;

import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequiredDiscordUserPermsCommandPropertyTest {
    private CommandParameters getCommandParameters(final boolean isGuildCommand, final Permission... permissions) {
        final Member member = mock(Member.class);
        final EnumSet<Permission> permission = EnumSet.noneOf(Permission.class);
        permission.addAll(Arrays.asList(permissions));
        when(member.getPermissions()).thenReturn(permission);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(isGuildCommand);
        when(commandParameters.getGuildMember()).thenReturn(member);

        return commandParameters;
    }

    @Test
    void getValueEmpty() {
        final RequiredDiscordUserPermsCommandProperty property = new RequiredDiscordUserPermsCommandProperty();
        assertThat(property.getValue()).isEmpty();
    }

    @Test
    void getValue() {
        final Permission[] input = {Permission.MESSAGE_MANAGE, Permission.MESSAGE_MENTION_EVERYONE};
        final RequiredDiscordUserPermsCommandProperty property = new RequiredDiscordUserPermsCommandProperty(input);
        assertThat(property.getValue()).containsExactly(input);
    }

    @Test
    void onPermissionCheck_private_message() {
        final RequiredDiscordUserPermsCommandProperty property = new RequiredDiscordUserPermsCommandProperty();
        final CommandParameters commandParameters = this.getCommandParameters(false);
        final AbstractCommand command = mock(AbstractCommand.class);

        assertThat(property.onPermissionCheck(command, commandParameters)).isTrue();
    }

    @Test
    void onPermissionCheck_guild_message() {
        final Permission[] input = {Permission.MESSAGE_MANAGE, Permission.MESSAGE_MENTION_EVERYONE};
        final RequiredDiscordUserPermsCommandProperty property = new RequiredDiscordUserPermsCommandProperty(input);
        final CommandParameters commandParameters = this.getCommandParameters(true, input);
        final AbstractCommand command = mock(AbstractCommand.class);

        assertThat(property.onPermissionCheck(command, commandParameters)).isTrue();
    }

    @Test
    void onPermissionCheck_guild_message_fail() {
        final Permission[] input = {Permission.MESSAGE_MANAGE, Permission.MESSAGE_MENTION_EVERYONE};
        final RequiredDiscordUserPermsCommandProperty property = new RequiredDiscordUserPermsCommandProperty(input);
        final CommandParameters commandParameters = this.getCommandParameters(true);
        final AbstractCommand command = mock(AbstractCommand.class);

        assertThat(property.onPermissionCheck(command, commandParameters)).isFalse();
    }
}