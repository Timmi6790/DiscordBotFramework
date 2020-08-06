package de.timmi6790.discord_framework.modules.command.properties;

import de.timmi6790.discord_framework.modules.command.CommandProperty;
import net.dv8tion.jda.api.Permission;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.util.List;

public class RequiredDiscordBotPermsCommandProperty extends CommandProperty<List<Permission>> {
    private final Permission[] permissions;

    public RequiredDiscordBotPermsCommandProperty(final Permission... permissions) {
        this.permissions = permissions;
    }

    @Override
    public List<Permission> getValue() {
        return Arrays.asList(this.permissions);
    }
}
