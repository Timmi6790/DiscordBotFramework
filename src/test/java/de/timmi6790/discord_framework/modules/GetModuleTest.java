package de.timmi6790.discord_framework.modules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetModuleTest {
    @Test
    void getModuleClassFirstExtend() {
        final FirstExtend firstExtend = new FirstExtend();
        assertThat(firstExtend.getModuleClass()).isEqualTo(ExampleModule.class);
    }

    @Test
    void getModuleClassThirdExtend() {
        final ThirdExtend thirdExtend = new ThirdExtend();
        assertThat(thirdExtend.getModuleClass()).isEqualTo(ExampleModule.class);
    }

    @Test
    void getModuleClassIllegal() {
        final IllegalExtend illegalExtend = new IllegalExtend();
        assertThrows(IllegalStateException.class, illegalExtend::getModuleClass);
    }

    private static class FirstExtend extends GetModule<ExampleModule> {

    }

    private static class SecondExtend extends FirstExtend {

    }

    private static class ThirdExtend extends SecondExtend {

    }

    private static class IllegalExtend extends GetModule {

    }
}