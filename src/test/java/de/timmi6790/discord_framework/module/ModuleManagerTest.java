package de.timmi6790.discord_framework.module;

import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import de.timmi6790.discord_framework.module.exceptions.ModuleNotFoundException;
import de.timmi6790.discord_framework.module.provider.providers.InternalModuleProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleManagerTest {
    private ModuleManager getModuleManager() {
        return new ModuleManager();
    }

    @Test
    void registerModule() {
        final ModuleManager moduleManager = this.getModuleManager();
        assertThat(moduleManager.registerModule(ExampleModule.class)).isTrue();
        assertThat(moduleManager.getModules(ModuleStatus.REGISTERED)).hasSize(1);
    }

    @Test
    void register_already_registered_module() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(AbstractModule.class);
        assertThat(moduleManager.registerModule(AbstractModule.class)).isFalse();
    }

    @Test
    void registerModules() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModules(
                ExampleModule.class,
                ExampleModule2.class
        );
        assertThat(moduleManager.getModules(ModuleStatus.REGISTERED)).hasSize(2);
    }

    @Test
    void getModule() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(ExampleModule.class);

        final Optional<ExampleModule> found = moduleManager.getModule(ExampleModule.class);
        assertThat(found).isPresent();
    }

    @Test
    void getModuleOrThrow() {
        final ModuleManager moduleManager = this.getModuleManager();
        assertThrows(ModuleNotFoundException.class, () -> moduleManager.getModuleOrThrow(ExampleModule.class));

        moduleManager.registerModule(ExampleModule.class);
        final Optional<ExampleModule> found = moduleManager.getModule(ExampleModule.class);
        assertThat(found).isPresent();
    }

    @Test
    void initialize() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(ExampleModule.class);
        moduleManager.initialize(ExampleModule.class);

        final ExampleModule exampleModule = moduleManager.getModuleOrThrow(ExampleModule.class);
        assertThat(moduleManager.getModules(ModuleStatus.INITIALIZED))
                .containsExactly(exampleModule);
    }

    @Test
    void initialize_missing_dependency() throws TopicalSortCycleException {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(DependencyModule.class);
        moduleManager.initializeAll();

        assertThat(moduleManager.getModules(ModuleStatus.INITIALIZED)).isEmpty();
    }

    @Test
    void initialize_with_dependency() throws TopicalSortCycleException {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModules(
                DependencyModule.class,
                ExampleModule.class,
                ExampleModule2.class
        );
        moduleManager.initializeAll();

        assertThat(moduleManager.getModules(ModuleStatus.INITIALIZED)).hasSize(3);
    }

    @Test
    void initialize_while_started() throws TopicalSortCycleException {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(ExampleModule.class);
        moduleManager.initializeAll();
        moduleManager.startAll();

        assertThat(moduleManager.initialize(ExampleModule.class)).isFalse();
    }

    @Test
    void initialize_while_initialized() throws TopicalSortCycleException {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(ExampleModule.class);
        moduleManager.initializeAll();

        assertThat(moduleManager.initialize(ExampleModule.class)).isFalse();
    }

    @Test
    void start() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(ExampleModule.class);

        moduleManager.initialize(ExampleModule.class);
        moduleManager.start(ExampleModule.class);

        final ExampleModule module = moduleManager.getModuleOrThrow(ExampleModule.class);
        assertThat(moduleManager.getModules(ModuleStatus.STARTED)).containsExactly(module);
    }

    @Test
    void start_without_initialize() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(ExampleModule.class);

        assertThat(moduleManager.start(ExampleModule.class)).isFalse();
        assertThat(moduleManager.getModules(ModuleStatus.STARTED)).isEmpty();
    }

    @Test
    void initializeAll() throws TopicalSortCycleException {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModules(
                ExampleModule.class,
                ExampleModule2.class
        );
        moduleManager.initializeAll();

        final ExampleModule module1 = moduleManager.getModuleOrThrow(ExampleModule.class);
        final ExampleModule2 module2 = moduleManager.getModuleOrThrow(ExampleModule2.class);
        assertThat(moduleManager.getModules(ModuleStatus.INITIALIZED)).containsOnly(module1, module2);
    }

    @Test
    void startAll() throws TopicalSortCycleException {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModules(
                ExampleModule.class,
                ExampleModule2.class
        );

        moduleManager.initializeAll();
        moduleManager.startAll();

        final ExampleModule module1 = moduleManager.getModuleOrThrow(ExampleModule.class);
        final ExampleModule2 module2 = moduleManager.getModuleOrThrow(ExampleModule2.class);
        assertThat(moduleManager.getModules(ModuleStatus.STARTED)).containsOnly(module1, module2);
    }


    @Test
    void stopModule() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(ExampleModule.class);

        moduleManager.initialize(ExampleModule.class);
        moduleManager.start(ExampleModule.class);
        moduleManager.stopModule(ExampleModule.class);

        assertThat(moduleManager.getModules(ModuleStatus.STARTED))
                .isEmpty();
        assertThat(moduleManager.getModules(ModuleStatus.INITIALIZED))
                .isEmpty();
    }

    @Test
    void stop_not_loaded_module() {
        final ModuleManager moduleManager = this.getModuleManager();
        assertThat(moduleManager.stopModule(ExampleModule.class)).isFalse();
    }

    @Test
    void load_modules_empty_providers() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.loadModules();

        final List<ModuleInfo> moduleInfos = moduleManager.getModuleInfos(ModuleStatus.REGISTERED);
        assertThat(moduleInfos).isEmpty();
    }

    @Test
    void load_modules_internal_provider() {
        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.addModuleProviders(
                new InternalModuleProvider()
        );

        moduleManager.loadModules();

        final List<ModuleInfo> moduleInfos = moduleManager.getModuleInfos(ModuleStatus.REGISTERED);
        assertThat(moduleInfos).isNotEmpty();
    }

    private static class ExampleModule extends AbstractModule {
        public ExampleModule() {
            super("Example");
        }

        @Override
        public boolean onInitialize() {
            return super.onInitialize();
        }
    }

    private static class ExampleModule2 extends ExampleModule {
        public ExampleModule2() {
            super();
        }
    }

    private static class DependencyModule extends AbstractModule {
        public DependencyModule() {
            super("Test");

            this.addDependenciesAndLoadAfter(
                    ExampleModule.class,
                    ExampleModule2.class
            );
        }
    }
}
