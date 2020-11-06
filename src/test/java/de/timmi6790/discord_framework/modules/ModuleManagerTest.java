package de.timmi6790.discord_framework.modules;

import de.timmi6790.discord_framework.exceptions.ModuleNotFoundException;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import org.junit.jupiter.api.Test;
import org.tinylog.Logger;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class ModuleManagerTest {
    private ModuleManager getModuleManager() {
        return new ModuleManager(Logger.tag(""));
    }

    @Test
    void registerModule() {
        final ModuleManager moduleManager = this.getModuleManager();
        final AbstractModule module = new ExampleModule();
        assertThat(moduleManager.registerModule(module)).isTrue();
        assertThat(moduleManager.getLoadedModules()).hasSize(1);
    }

    @Test
    void register_already_registered_module() {
        final ModuleManager moduleManager = this.getModuleManager();
        final AbstractModule module = new ExampleModule();
        moduleManager.registerModule(module);
        assertThat(moduleManager.registerModule(module)).isFalse();
    }

    @Test
    void registerModules() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModules(
                new ExampleModule(),
                new ExampleModule2()
        );
        assertThat(moduleManager.getLoadedModules()).hasSize(2);
    }

    @Test
    void getModule() {
        final ModuleManager moduleManager = this.getModuleManager();
        final ExampleModule module = new ExampleModule();
        moduleManager.registerModule(module);

        final Optional<ExampleModule> found = moduleManager.getModule(ExampleModule.class);
        assertThat(found).isPresent().hasValue(module);
    }

    @Test
    void getModuleOrThrow() {
        final ModuleManager moduleManager = this.getModuleManager();
        assertThrows(ModuleNotFoundException.class, () -> moduleManager.getModuleOrThrow(ExampleModule.class));

        moduleManager.registerModule(new ExampleModule());
        final Optional<ExampleModule> found = moduleManager.getModule(ExampleModule.class);
        assertThat(found).isPresent();
    }

    @Test
    void initialize() {
        final ExampleModule module = spy(new ExampleModule());

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(module);
        moduleManager.initialize(ExampleModule.class);

        verify(module).onInitialize();
        assertThat(moduleManager.getInitializedModules()).containsExactly(ExampleModule.class);
    }

    @Test
    void initialize_missing_dependency() throws TopicalSortCycleException {
        final ExampleModule dependencyModule = spy(new ExampleModule());
        dependencyModule.addDependenciesAndLoadAfter(ExampleModule2.class);

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(dependencyModule);
        moduleManager.initializeAll();

        assertThat(moduleManager.getInitializedModules()).isEmpty();
    }

    @Test
    void initialize_while_started() throws TopicalSortCycleException {
        final ExampleModule exampleModule = new ExampleModule();

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(exampleModule);
        moduleManager.initializeAll();
        moduleManager.startAll();

        assertThat(moduleManager.initialize(ExampleModule.class)).isFalse();
    }

    @Test
    void initialize_while_initialized() throws TopicalSortCycleException {
        final ExampleModule exampleModule = new ExampleModule();

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(exampleModule);
        moduleManager.initializeAll();

        assertThat(moduleManager.initialize(ExampleModule.class)).isFalse();
    }

    @Test
    void start() {
        final ExampleModule module = spy(new ExampleModule());

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(module);

        moduleManager.initialize(ExampleModule.class);
        moduleManager.start(ExampleModule.class);

        verify(module).onEnable();
        assertThat(moduleManager.getStartedModules()).containsExactly(ExampleModule.class);
    }

    @Test
    void start_without_initialize() {
        final ExampleModule module = new ExampleModule();

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(module);

        assertThat(moduleManager.start(ExampleModule.class)).isFalse();
        assertThat(moduleManager.getStartedModules()).isEmpty();
    }

    @Test
    void initializeAll() throws TopicalSortCycleException {
        final ExampleModule module1 = spy(new ExampleModule());
        final ExampleModule2 module2 = spy(new ExampleModule2());

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModules(
                module1,
                module2
        );
        moduleManager.initializeAll();

        verify(module1).onInitialize();
        verify(module2).onInitialize();
        assertThat(moduleManager.getInitializedModules())
                .containsExactlyInAnyOrder(ExampleModule.class, ExampleModule2.class);
    }

    @Test
    void startAll() throws TopicalSortCycleException {
        final ExampleModule module1 = spy(new ExampleModule());
        final ExampleModule2 module2 = spy(new ExampleModule2());

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModules(
                module1,
                module2
        );

        moduleManager.initializeAll();
        moduleManager.startAll();

        verify(module1).onEnable();
        verify(module2).onEnable();

        assertThat(moduleManager.getStartedModules()).containsExactlyInAnyOrder(ExampleModule.class, ExampleModule2.class);
    }


    @Test
    void stopModule() {
        final ExampleModule module = spy(new ExampleModule());

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(module);

        moduleManager.initialize(ExampleModule.class);
        moduleManager.start(ExampleModule.class);
        moduleManager.stopModule(ExampleModule.class);

        verify(module).onDisable();
        assertThat(moduleManager.getStartedModules())
                .isEmpty();
        assertThat(moduleManager.getInitializedModules())
                .isEmpty();
    }

    @Test
    void stop_not_loaded_module() {
        final ModuleManager moduleManager = this.getModuleManager();
        assertThat(moduleManager.stopModule(ExampleModule.class)).isFalse();
    }

    private static class ExampleModule extends AbstractModule {
        public ExampleModule() {
            super("Example");
        }

        @Override
        public void onInitialize() {
        }

        @Override
        public void onEnable() {

        }

        @Override
        public void onDisable() {

        }
    }

    private static class ExampleModule2 extends ExampleModule {

    }
}
