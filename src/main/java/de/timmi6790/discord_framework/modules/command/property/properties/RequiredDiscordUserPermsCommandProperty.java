package de.timmi6790.discord_framework.modules.command.property.properties;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.property.CommandProperty;
import lombok.NonNull;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class RequiredDiscordUserPermsCommandProperty implements CommandProperty<EnumSet<Permission>> {
    private final EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);

    public RequiredDiscordUserPermsCommandProperty(final Permission... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
    }

    @Override
    public EnumSet<Permission> getValue() {
        return this.permissions;
    }

    @Override
    public boolean onPermissionCheck(@NonNull final AbstractCommand command,
                                     @NonNull final CommandParameters commandParameters) {
        if (commandParameters.isGuildCommand()) {
            final Set<Permission> userPermissions = commandParameters.getGuildMember().getPermissions();
            for (final Permission requiredPermission : this.getValue()) {
                if (!userPermissions.contains(requiredPermission)) {
                    return false;
                }
            }
        }

        return true;
    }
}
