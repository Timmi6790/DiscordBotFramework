package de.timmi6790.discord_framework.modules.new_module_manager;

import lombok.Data;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Data

public class ModuleInfo {
    private ModuleStatus status = ModuleStatus.INITIALIZED;
    private Module module;

    public String getModuleName() {
        return this.module.getName();
    }

    public String getModuleVersion() {
        return this.module.getVersion();
    }

    public GatewayIntent[] getGatewayIntents() {
        return this.module.getGatewayIntents();
    }
}
