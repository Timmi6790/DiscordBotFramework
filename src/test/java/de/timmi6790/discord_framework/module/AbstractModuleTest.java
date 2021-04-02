package de.timmi6790.discord_framework.module;

import net.dv8tion.jda.api.requests.GatewayIntent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractModuleTest {
    @Test
    void addGatewayIntents() {
        final GatewayIntent[] intents = new GatewayIntent[]{
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.GUILD_MEMBERS
        };
        final TestModule module = new TestModule();
        module.addDiscordGatewayIntents(intents);

        assertThat(module.getRequiredGatewayIntents()).containsExactlyInAnyOrder(intents);
    }

    @Test
    void addDependencies() {
        final TestModule module = new TestModule();
        module.addDependencies(TestModule2.class);

        assertThat(module.getDependencies()).containsExactly(TestModule2.class);
    }

    @Test
    void addDependenciesAndLoadAfter() {
        final TestModule module = new TestModule();
        module.addDependenciesAndLoadAfter(TestModule2.class);

        assertThat(module.getDependencies()).containsExactly(TestModule2.class);

        assertThat(module.getLoadAfterDependencies()).containsExactly(TestModule2.class);
    }

    @Test
    void addLoadAfter() {
        final TestModule module = new TestModule();
        module.addLoadAfterDependencies(TestModule2.class);

        assertThat(module.getLoadAfterDependencies()).containsExactly(TestModule2.class);
    }

    public static class TestModule extends AbstractModule {
        TestModule() {
            super("Test");
        }
    }

    public static class TestModule2 extends AbstractModule {
        TestModule2() {
            super("Test2");
        }
    }
}