package de.timmi6790.discord_framework.module.modules.command.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RequiredDiscordUserPermsPropertyTest {
    void onPermissionCheck(final boolean expectedReturnValue,
                           final boolean guildCommand,
                           final EnumSet<Permission> userPermissions,
                           final EnumSet<Permission> requiredPermissions) {
        final Member member = mock(Member.class);
        when(member.getPermissions()).thenReturn(userPermissions);

        final CommandParameters commandParameters = mock(CommandParameters.class);
        when(commandParameters.isGuildCommand()).thenReturn(guildCommand);
        when(commandParameters.getGuildMember()).thenReturn(member);

        final Command command = mock(Command.class);

        final RequiredDiscordUserPermsProperty property = spy(new RequiredDiscordUserPermsProperty(requiredPermissions.toArray(new Permission[0])));

        final boolean returnValue = property.onPermissionCheck(command, commandParameters);
        assertThat(returnValue).isEqualTo(expectedReturnValue);
    }

    @Test
    void getValue() {
        final Permission[] permissions = new Permission[]{Permission.MESSAGE_SEND, Permission.ADMINISTRATOR};
        final RequiredDiscordUserPermsProperty property = new RequiredDiscordUserPermsProperty(permissions);
        assertThat(property.getValue()).containsExactlyInAnyOrder(permissions);
    }

    @Test
    void onPermissionCheck_private_message() {
        this.onPermissionCheck(
                true,
                false,
                EnumSet.noneOf(Permission.class),
                EnumSet.allOf(Permission.class)
        );
    }

    @Test
    void onPermissionCheck_guild_missing_perms() {
        this.onPermissionCheck(
                false,
                true,
                EnumSet.noneOf(Permission.class),
                EnumSet.allOf(Permission.class)
        );
    }

    @Test
    void onPermissionCheck_guild_valid_perms() {
        this.onPermissionCheck(
                true,
                true,
                EnumSet.allOf(Permission.class),
                EnumSet.noneOf(Permission.class)
        );
    }
}