package de.timmi6790.discord_framework.module.modules.command.property.properties.info;

import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.EnumSet;

public class RequiredDiscordBotPermsProperty implements CommandProperty<EnumSet<Permission>> {
    private final EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);

    public RequiredDiscordBotPermsProperty(final Permission... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
    }

    @Override
    public EnumSet<Permission> getValue() {
        return this.permissions;
    }
}
