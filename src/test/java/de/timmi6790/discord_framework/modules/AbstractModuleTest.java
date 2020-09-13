package de.timmi6790.discord_framework.modules;

import net.dv8tion.jda.api.requests.GatewayIntent;
import org.junit.jupiter.api.Test;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractModuleTest {
    @Test
    void addGatewayIntents() {
        final GatewayIntent[] intents = new GatewayIntent[]{GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MEMBERS};
        final TestModule module = new TestModule();
        module.addGatewayIntents(intents);

        assertThat(module.getRequiredGatewayIntents())
                .hasSize(intents.length)
                .containsAll(Arrays.asList(intents));
    }

    @Test
    void addDependencies() {
        final TestModule module = new TestModule();
        module.addDependencies(TestModule2.class);

        assertThat(module.getDependencies())
                .hasSize(1)
                .contains(TestModule2.class);
    }

    @Test
    void addDependenciesAndLoadAfter() {
        final TestModule module = new TestModule();
        module.addDependenciesAndLoadAfter(TestModule2.class);

        assertThat(module.getDependencies())
                .hasSize(1)
                .contains(TestModule2.class);

        assertThat(module.getLoadAfter())
                .hasSize(1)
                .contains(TestModule2.class);
    }

    @Test
    void addLoadAfter() {
        final TestModule module = new TestModule();
        module.addLoadAfter(TestModule2.class);

        assertThat(module.getLoadAfter())
                .hasSize(1)
                .contains(TestModule2.class);
    }

    @Test
    void addLoadBefore() {
        final TestModule module = new TestModule();
        module.addLoadBefore(TestModule2.class);

        assertThat(module.getLoadBefore())
                .hasSize(1)
                .contains(TestModule2.class);
    }

    public static class TestModule extends AbstractModule {
        public TestModule() {
            super("Test");
        }
    }

    public static class TestModule2 extends AbstractModule {
        public TestModule2() {
            super("Test2");
        }
    }
}