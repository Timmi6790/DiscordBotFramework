package de.timmi6790.discord_framework.module.modules.stat;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.module.ModuleManager;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class StatModuleTest {
    private static final AtomicInteger SETTING_NAME_NUMBER = new AtomicInteger(0);

    private static final StatModule statModule = spy(new StatModule());

    private static String generateStatName() {
        return "Achievement" + SETTING_NAME_NUMBER.getAndIncrement();
    }

    @BeforeAll
    static void setup() {
        final ModuleManager moduleManager = mock(ModuleManager.class);
        final PermissionsModule permissionsModule = spy(new PermissionsModule());
        doReturn(permissionsModule).when(moduleManager).getModuleOrThrow(PermissionsModule.class);
        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);

        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getModuleManager()).thenReturn(moduleManager);
        when(moduleManager.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);

        final EventModule eventModule = mock(EventModule.class);
        when(moduleManager.getModuleOrThrow(EventModule.class)).thenReturn(eventModule);

        doReturn(moduleManager).when(permissionsModule).getModuleManager();
        doReturn(moduleManager).when(statModule).getModuleManager();

        permissionsModule.onInitialize();
        statModule.onInitialize();
    }

    private void hasStats(final AbstractStat... stats) {
        for (final AbstractStat stat : stats) {
            assertThat(statModule.getStat(stat.getDatabaseId())).hasValue(stat);
            assertThat(statModule.getStat(stat.getName())).hasValue(stat);
        }
    }

    @Test
    void hasStat() {
        final TestStat stat = new TestStat(generateStatName());
        statModule.registerStat(statModule, stat);
        assertThat(statModule.hasStat(stat)).isTrue();
    }

    @Test
    void registerStats() {
        final TestStat stat1 = new TestStat(generateStatName());
        final TestStat stat2 = new TestStat(generateStatName());
        statModule.registerStats(statModule, stat1, stat2);
        this.hasStats(stat1, stat2);
    }

    @Test
    void registerStat() {
        final TestStat stat = new TestStat(generateStatName());
        statModule.registerStat(statModule, stat);
        this.hasStats(stat);
    }

    @Test
    void registerStat_duplicate() {
        final TestStat stat = new TestStat(generateStatName());
        assertThat(statModule.registerStat(statModule, stat)).isTrue();
        assertThat(statModule.registerStat(statModule, stat)).isFalse();
    }

    @Test
    void getStat_name_empty() {
        assertThat(statModule.getStat(generateStatName())).isEmpty();
    }

    private static class TestStat extends AbstractStat {
        public TestStat(final String name) {
            super(name);
        }
    }
}