package de.timmi6790.discord_framework.modules.dsgvo.repository;

import de.timmi6790.discord_framework.modules.dsgvo.UserData;

public interface DsgvoRepository {
    UserData getUserData(final long discordUserId);
}
