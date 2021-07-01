package de.timmi6790.discord_framework.module.modules.setting.repository.postgres;

import de.timmi6790.discord_framework.module.modules.setting.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

@RequiredArgsConstructor
public class SettingPostgresRepository implements SettingRepository {
    private static final String GET_SETTING = "SELECT id FROM settings WHERE setting_name = :settingName LIMIT 1;";
    private static final String INSERT_SETTING = "INSERT INTO settings(setting_name) VALUES(:settingName) RETURNING id;";

    private final Jdbi database;

    @Override
    public Optional<Integer> getSettingId(final String internalName) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_SETTING)
                        .bind("settingName", internalName)
                        .mapTo(Integer.class)
                        .findFirst()
        );
    }

    @Override
    public int createSetting(final String internalName) {
        return this.database.withHandle(handle ->
                handle.createQuery(INSERT_SETTING)
                        .bind("settingName", internalName)
                        .mapTo(Integer.class)
                        .first()
        );
    }
}
