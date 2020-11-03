package de.timmi6790.discord_framework.modules.setting.repository;

public interface SettingRepository {
    int retrieveOrCreateSettingId(String internalName);
}
