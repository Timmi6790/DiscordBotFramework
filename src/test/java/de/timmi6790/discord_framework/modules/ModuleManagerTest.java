package de.timmi6790.discord_framework.modules;

import de.timmi6790.discord_framework.exceptions.ModuleGetException;
import de.timmi6790.discord_framework.exceptions.TopicalSortCycleException;
import lombok.NonNull;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleManagerTest {
    private ModuleManager getModuleManager() {
        return new ModuleManager();
    }

    @Test
    void registerModule() {
        final ModuleManager moduleManager = this.getModuleManager();
        final AbstractModule module = new ExampleModule();
        assertThat(moduleManager.registerModule(module)).isTrue();
        assertThat(moduleManager.getLoadedModules()).hasSize(1);
    }

    @Test
    void registerAlreadyRegisteredModule() {
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
        assertThrows(ModuleGetException.class, () -> moduleManager.getModuleOrThrow(ExampleModule.class));

        moduleManager.registerModule(new ExampleModule());
        final Optional<ExampleModule> found = moduleManager.getModule(ExampleModule.class);
        assertThat(found).isPresent();
    }

    @Test
    void initialize() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final CountdownModule countdownModule = new CountdownModule(countDownLatch);
        countdownModule.setCallOnInitialize(true);

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(countdownModule);

        moduleManager.initialize(CountdownModule.class);
        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(moduleManager.getInitializedModules())
                .hasSize(1)
                .contains(CountdownModule.class);
    }

    @Test
    void initializeMissingDependency() throws TopicalSortCycleException {
        final DependencyModule dependencyModule = new DependencyModule();
        final ModuleManager moduleManager = new ModuleManager();
        moduleManager.registerModule(dependencyModule);
        moduleManager.initializeAll();

        assertThat(moduleManager.getInitializedModules()).isEmpty();
    }

    @Test
    void initializeWhileStarted() throws TopicalSortCycleException {
        final ExampleModule exampleModule = new ExampleModule();
        final ModuleManager moduleManager = new ModuleManager();
        moduleManager.registerModule(exampleModule);
        moduleManager.initializeAll();
        moduleManager.startAll();

        assertThat(moduleManager.initialize(ExampleModule.class)).isFalse();
    }

    @Test
    void initializeWhileInitilized() throws TopicalSortCycleException {
        final ExampleModule exampleModule = new ExampleModule();
        final ModuleManager moduleManager = new ModuleManager();
        moduleManager.registerModule(exampleModule);
        moduleManager.initializeAll();

        assertThat(moduleManager.initialize(ExampleModule.class)).isFalse();
    }

    @Test
    void start() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final CountdownModule countdownModule = new CountdownModule(countDownLatch);
        countdownModule.setCallOnEnable(true);

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(countdownModule);

        moduleManager.initialize(CountdownModule.class);
        moduleManager.start(CountdownModule.class);

        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(moduleManager.getStartedModules())
                .hasSize(1)
                .contains(CountdownModule.class);
    }

    @Test
    void startWithoutInitialize() {
        final ExampleModule countdownModule = new ExampleModule();

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(countdownModule);

        assertThat(moduleManager.start(CountdownModule.class))
                .isFalse();
        assertThat(moduleManager.getStartedModules())
                .isEmpty();
    }

    @Test
    void initializeAll() throws InterruptedException, TopicalSortCycleException {
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        final CountdownModule countdownModule = new CountdownModule(countDownLatch);
        countdownModule.setCallOnInitialize(true);

        final CountdownModule countdownModule2 = new CountdownModule2(countDownLatch);
        countdownModule2.setCallOnInitialize(true);

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModules(
                countdownModule,
                countdownModule2
        );

        moduleManager.initializeAll();

        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(moduleManager.getInitializedModules())
                .hasSize(2)
                .contains(CountdownModule.class)
                .contains(CountdownModule2.class);
    }

    @Test
    void startAll() throws TopicalSortCycleException, InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final CountdownModule countdownModule = new CountdownModule(countDownLatch);
        countdownModule.setCallOnEnable(true);

        final CountdownModule countdownModule2 = new CountdownModule2(countDownLatch);
        countdownModule.setCallOnEnable(true);

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModules(
                countdownModule,
                countdownModule2
        );

        moduleManager.initializeAll();
        moduleManager.startAll();

        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(moduleManager.getStartedModules())
                .hasSize(2)
                .contains(CountdownModule.class)
                .contains(CountdownModule2.class);
    }


    @Test
    void stopModule() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final CountdownModule countdownModule = new CountdownModule(countDownLatch);
        countdownModule.setCallOnDisable(true);

        final ModuleManager moduleManager = this.getModuleManager();
        moduleManager.registerModule(countdownModule);

        moduleManager.initialize(CountdownModule.class);
        moduleManager.start(CountdownModule.class);
        moduleManager.stopModule(CountdownModule.class);

        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(moduleManager.getStartedModules())
                .isEmpty();
        assertThat(moduleManager.getInitializedModules())
                .isEmpty();
    }

    @Test
    void stopNotLoadedModule() {
        final ModuleManager moduleManager = this.getModuleManager();
        assertThat(moduleManager.stopModule(ExampleModule.class)).isFalse();
    }

    @Setter
    private static class CountdownModule extends AbstractModule {
        private final CountDownLatch countDownLatch;
        private boolean callOnInitialize = false;
        private boolean callOnEnable = false;
        private boolean callOnDisable = false;

        public CountdownModule(final CountDownLatch countDownLatch) {
            this("CountdownModule", countDownLatch);
        }

        public CountdownModule(@NonNull final String moduleName, @NonNull final CountDownLatch countDownLatch) {
            super(moduleName);

            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onEnable() {
            if (this.callOnEnable) {
                this.countDownLatch.countDown();
            }
        }

        @Override
        public void onInitialize() {
            if (this.callOnInitialize) {
                this.countDownLatch.countDown();
            }
        }

        @Override
        public void onDisable() {
            if (this.callOnDisable) {
                this.countDownLatch.countDown();
            }
        }
    }

    private static class CountdownModule2 extends CountdownModule {
        public CountdownModule2(final CountDownLatch countDownLatch) {
            super("CountdownModule2", countDownLatch);
        }
    }

    private static class ExampleModule2 extends ExampleModule {

    }

    private static class DependencyModule extends AbstractModule {
        public DependencyModule() {
            super("DependencyModule");

            this.addDependenciesAndLoadAfter(
                    ExampleModule2.class
            );
        }
    }
}
