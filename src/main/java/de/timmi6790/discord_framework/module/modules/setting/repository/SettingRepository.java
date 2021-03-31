package de.timmi6790.discord_framework.module.modules.setting.repository;

public interface SettingRepository {
    int retrieveOrCreateSettingId(String internalName);
}
