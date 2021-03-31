package de.timmi6790.discord_framework.module.modules.stat.repository;

public interface StatRepository {
    int retrieveOrCreateSettingId(String internalName);
}
