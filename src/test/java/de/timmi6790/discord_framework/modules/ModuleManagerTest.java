package de.timmi6790.discord_framework.modules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModuleManagerTest {
    private ModuleManager getModuleManager() {
        return new ModuleManager();
    }

    @Test
    void registerModule() {
        final ModuleManager commandManager = this.getModuleManager();
        final AbstractModule module = new ExampleModule();
        assertThat(commandManager.registerModule(module)).isTrue();
    }

    @Test
    void getModuleByClass() {
        final ModuleManager commandManager = this.getModuleManager();
        final ExampleModule module = new ExampleModule();
        commandManager.registerModule(module);

        final ExampleModule found = commandManager.getModule(ExampleModule.class).orElseThrow(RuntimeException::new);
        assertThat(module).isEqualTo(found);
    }
}
