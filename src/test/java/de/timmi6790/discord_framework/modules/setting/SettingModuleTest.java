package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.setting.settings.BooleanSetting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.spy;

class SettingModuleTest {
    private static final AtomicInteger SETTING_NAME_NUMBER = new AtomicInteger(0);

    private static final PermissionsModule permissionsModule = spy(PermissionsModule.class);
    private static final SettingModule settingsModule = spy(SettingModule.class);

    private static String generateSettingName() {
        return "Setting" + SETTING_NAME_NUMBER.getAndIncrement();
    }

    @BeforeAll
    static void setup() {

    }

    private void hasSetting(final AbstractSetting<?> setting) {
        assertThat(settingsModule.getSetting(setting.getStatName())).hasValue(setting);
        for (final String aliasName : setting.getAliasNames()) {
            assertThat(settingsModule.getSetting(aliasName)).hasValue(setting);
        }
    }

    @Test
    void registerSetting() {
        final TestBooleanSetting setting = new TestBooleanSetting(
                generateSettingName(),
                true,
                generateSettingName(),
                generateSettingName()
        );

        settingsModule.registerSettings(settingsModule, setting);
        this.hasSetting(setting);
    }

    @Test
    void registerSettings() {
        final List<AbstractSetting<?>> settings = new ArrayList<>();
        for (int count = 0; 10 > count; count++) {
            settings.add(new TestBooleanSetting(
                    generateSettingName(),
                    true,
                    generateSettingName()
            ));
        }

        for (final AbstractSetting<?> setting : settings) {
            settingsModule.registerSetting(settingsModule, setting);
        }

        for (final AbstractSetting<?> setting : settings) {
            this.hasSetting(setting);
        }
    }

    @Test
    void getSetting_empty() {
        final String settingName = generateSettingName();
        assertThat(settingsModule.getSetting(settingName)).isEmpty();
    }

    @Test
    void getSetting_class() {
        final UniqueBooleanSetting setting = new UniqueBooleanSetting();
        settingsModule.registerSetting(settingsModule, setting);

        final Optional<UniqueBooleanSetting> settingFound = (Optional<UniqueBooleanSetting>) settingsModule.getSetting(UniqueBooleanSetting.class);
        assertThat(settingFound).hasValue(setting);
    }

    @Test
    void getSetting_class_empty() {
        assertThat(settingsModule.getSetting(EmptyUniqueBooleanSetting.class)).isEmpty();
    }

    private static class TestBooleanSetting extends BooleanSetting {
        public TestBooleanSetting(final String name, final boolean defaultValue, final String... aliasNames) {
            super(name, "", defaultValue, aliasNames);
        }
    }

    private static class UniqueBooleanSetting extends BooleanSetting {
        public UniqueBooleanSetting() {
            super("UniqueBooleanSetting", "", true);
        }
    }

    private static class EmptyUniqueBooleanSetting extends BooleanSetting {
        public EmptyUniqueBooleanSetting() {
            super("EmptyUniqueBooleanSetting", "", true);
        }
    }
}