package de.timmi6790.discord_framework.modules.stat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.spy;

class StatModuleTest {
    private static final AtomicInteger SETTING_NAME_NUMBER = new AtomicInteger(0);

    private static final StatModule statModule = spy(StatModule.class);

    private static String generateStatName() {
        return "Achievement" + SETTING_NAME_NUMBER.getAndIncrement();
    }

    @BeforeAll
    static void setup() {

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