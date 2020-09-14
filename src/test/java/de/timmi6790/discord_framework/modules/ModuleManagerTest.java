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
        final ModuleManager commandManager = this.getModuleManager();
        final AbstractModule module = new ExampleModule();
        assertThat(commandManager.registerModule(module)).isTrue();
        assertThat(commandManager.getLoadedModules()).hasSize(1);
    }

    @Test
    void registerModules() {
        final ModuleManager commandManager = this.getModuleManager();
        commandManager.registerModules(
                new ExampleModule(),
                new ExampleModule2()
        );
        assertThat(commandManager.getLoadedModules()).hasSize(2);
    }

    @Test
    void getModule() {
        final ModuleManager commandManager = this.getModuleManager();
        final ExampleModule module = new ExampleModule();
        commandManager.registerModule(module);

        final Optional<ExampleModule> found = commandManager.getModule(ExampleModule.class);
        assertThat(found).isPresent().hasValue(module);
    }

    @Test
    void getModuleOrThrow() {
        final ModuleManager commandManager = this.getModuleManager();
        assertThrows(ModuleGetException.class, () -> commandManager.getModuleOrThrow(ExampleModule.class));

        commandManager.registerModule(new ExampleModule());
        final Optional<ExampleModule> found = commandManager.getModule(ExampleModule.class);
        assertThat(found).isPresent();
    }

    @Test
    void initialize() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final CountdownModule countdownModule = new CountdownModule(countDownLatch);
        countdownModule.setCallOnInitialize(true);

        final ModuleManager commandManager = this.getModuleManager();
        commandManager.registerModule(countdownModule);

        commandManager.initialize(CountdownModule.class);
        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(commandManager.getInitializedModules())
                .hasSize(1)
                .contains(CountdownModule.class);
    }

    @Test
    void start() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final CountdownModule countdownModule = new CountdownModule(countDownLatch);
        countdownModule.setCallOnEnable(true);

        final ModuleManager commandManager = this.getModuleManager();
        commandManager.registerModule(countdownModule);

        commandManager.initialize(CountdownModule.class);
        commandManager.start(CountdownModule.class);

        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(commandManager.getStartedModules())
                .hasSize(1)
                .contains(CountdownModule.class);
    }

    @Test
    void startWithoutInitialize() {
        final ExampleModule countdownModule = new ExampleModule();

        final ModuleManager commandManager = this.getModuleManager();
        commandManager.registerModule(countdownModule);

        assertThat(commandManager.start(CountdownModule.class))
                .isFalse();
        assertThat(commandManager.getStartedModules())
                .isEmpty();
    }

    @Test
    void initializeAll() throws InterruptedException, TopicalSortCycleException {
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        final CountdownModule countdownModule = new CountdownModule(countDownLatch);
        countdownModule.setCallOnInitialize(true);

        final CountdownModule countdownModule2 = new CountdownModule2(countDownLatch);
        countdownModule2.setCallOnInitialize(true);

        final ModuleManager commandManager = this.getModuleManager();
        commandManager.registerModules(
                countdownModule,
                countdownModule2
        );

        commandManager.initializeAll();

        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(commandManager.getInitializedModules())
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

        final ModuleManager commandManager = this.getModuleManager();
        commandManager.registerModules(
                countdownModule,
                countdownModule2
        );

        commandManager.initializeAll();
        commandManager.startAll();

        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(commandManager.getStartedModules())
                .hasSize(2)
                .contains(CountdownModule.class)
                .contains(CountdownModule2.class);
    }


    @Test
    void stopModule() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final CountdownModule countdownModule = new CountdownModule(countDownLatch);
        countdownModule.setCallOnDisable(true);

        final ModuleManager commandManager = this.getModuleManager();
        commandManager.registerModule(countdownModule);

        commandManager.initialize(CountdownModule.class);
        commandManager.start(CountdownModule.class);
        commandManager.stopModule(CountdownModule.class);

        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(countDownLatch.getCount()).isZero();
        assertThat(commandManager.getStartedModules())
                .isEmpty();
        assertThat(commandManager.getInitializedModules())
                .isEmpty();
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
}
