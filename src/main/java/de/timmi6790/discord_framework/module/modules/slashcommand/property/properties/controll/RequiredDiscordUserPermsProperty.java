package de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.controll;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.SlashCommandProperty;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class RequiredDiscordUserPermsProperty implements SlashCommandProperty<EnumSet<Permission>> {
    private final EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);

    public RequiredDiscordUserPermsProperty(final Permission... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
    }

    @Override
    public EnumSet<Permission> getValue() {
        return this.permissions;
    }

    @Override
    public boolean onPermissionCheck(final SlashCommand command,
                                     final SlashCommandParameters commandParameters) {
        if (commandParameters.isGuildCommand()) {
            final Set<Permission> userPermissions = commandParameters.getGuildMember().getPermissions();
            return userPermissions.containsAll(this.permissions);
        }

        return true;
    }
}
