package de.timmi6790.discord_framework.module;

import de.timmi6790.discord_framework.module.exceptions.ModuleUninitializedException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ModuleInfo {
    private ModuleStatus status = ModuleStatus.REGISTERED;
    private final Class<? extends AbstractModule> moduleClass;
    private final AbstractModule module;

    public AbstractModule getModule() {
        if (this.module == null) {
            throw new ModuleUninitializedException();
        }
        return this.module;
    }
}
