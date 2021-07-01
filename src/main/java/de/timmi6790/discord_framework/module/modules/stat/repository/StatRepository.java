package de.timmi6790.discord_framework.module.modules.stat.repository;

import java.util.Optional;

public interface StatRepository {
    Optional<Integer> getStatId(String internalName);

    int createStat(String internalName);
}
