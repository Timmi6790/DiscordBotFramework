package de.timmi6790.discord_framework.modules.stat.repository;

public interface StatRepository {
    int retrieveOrCreateSettingId(String internalName);
}
