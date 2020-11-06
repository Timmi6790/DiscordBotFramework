package de.timmi6790.discord_framework.modules.setting;

import de.timmi6790.discord_framework.AbstractIntegrationTest;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.ModuleManager;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.modules.setting.settings.BooleanSetting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class SettingModuleTest {
    private static final AtomicInteger SETTING_NAME_NUMBER = new AtomicInteger(0);

    private static final PermissionsModule permissionsModule = spy(new PermissionsModule());
    private static final SettingModule settingsModule = spy(new SettingModule());

    private static String generateSettingName() {
        return "Setting" + SETTING_NAME_NUMBER.getAndIncrement();
    }

    @BeforeAll
    static void setup() {
        final ModuleManager moduleManager = mock(ModuleManager.class);
        
        doReturn(permissionsModule).when(moduleManager).getModuleOrThrow(PermissionsModule.class);
        doReturn(AbstractIntegrationTest.databaseModule).when(moduleManager).getModuleOrThrow(DatabaseModule.class);

        final CommandModule commandModule = mock(CommandModule.class);
        when(commandModule.getModuleManager()).thenReturn(moduleManager);
        when(moduleManager.getModuleOrThrow(CommandModule.class)).thenReturn(commandModule);

        doReturn(moduleManager).when(permissionsModule).getModuleManager();
        doReturn(moduleManager).when(settingsModule).getModuleManager();

        try (final MockedStatic<DiscordBot> botMock = mockStatic(DiscordBot.class)) {
            final DiscordBot bot = mock(DiscordBot.class);
            when(bot.getModuleManager()).thenReturn(moduleManager);

            botMock.when(DiscordBot::getInstance).thenReturn(bot);
            permissionsModule.onInitialize();
            settingsModule.onInitialize();
        }
    }

    private void hasSetting(final AbstractSetting<?> setting) {
        assertThat(settingsModule.getSetting(setting.getName())).hasValue(setting);
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