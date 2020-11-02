package de.timmi6790.discord_framework.modules.user.repository;

import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.NonNull;

import java.util.Optional;

public interface UserDbRepository {
    UserDb create(final long discordId);

    Optional<UserDb> get(final long discordId);

    void delete(@NonNull final UserDb userDb);
}
