package de.timmi6790.discord_framework.module.modules.stat.repository.postgres;

import de.timmi6790.discord_framework.module.modules.stat.repository.StatRepository;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

@RequiredArgsConstructor
public class StatPostgresRepository implements StatRepository {
    private static final String GET_STAT = "SELECT id FROM stats WHERE stat_name = :statName LIMIT 1;";
    private static final String INSERT_STAT = "INSERT INTO stats(stat_name) VALUES(:statName) RETURNING id;";

    private final Jdbi database;

    @Override
    public Optional<Integer> getStatId(final String internalName) {
        return this.database.withHandle(handle ->
                handle.createQuery(GET_STAT)
                        .bind("statName", internalName)
                        .mapTo(Integer.class)
                        .findFirst()
        );
    }

    @Override
    public int createStat(final String internalName) {
        return this.database.withHandle(handle ->
                handle.createQuery(INSERT_STAT)
                        .bind("statName", internalName)
                        .mapTo(Integer.class)
                        .first()
        );
    }
}
