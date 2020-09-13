package de.timmi6790.discord_framework.modules.command.properties;

import de.timmi6790.discord_framework.modules.command.CommandProperty;
import net.dv8tion.jda.api.Permission;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.util.EnumSet;

public class RequiredDiscordBotPermsCommandProperty extends CommandProperty<EnumSet<Permission>> {
    private final EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);

    public RequiredDiscordBotPermsCommandProperty(final Permission... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
    }

    @Override
    public EnumSet<Permission> getValue() {
        return this.permissions;
    }
}
