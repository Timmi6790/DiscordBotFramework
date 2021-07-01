package de.timmi6790.discord_framework.module.modules.setting.repository;

import java.util.Optional;

public interface SettingRepository {
    Optional<Integer> getSettingId(String internalName);

    int createSetting(String internalName);
}
