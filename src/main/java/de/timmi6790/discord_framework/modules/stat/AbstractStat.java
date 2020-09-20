package de.timmi6790.discord_framework.modules.stat;

import de.timmi6790.commons.builders.MapBuilder;
import de.timmi6790.discord_framework.modules.database.DatabaseGetId;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.*;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
public class AbstractStat extends DatabaseGetId {
    private static final String STAT_NAME = "statName";

    private static final String GET_STAT_ID = "SELECT id FROM `stat` WHERE stat_name = :statName LIMIT 1;";
    private static final String INSERT_NEW_STAT = "INSERT INTO stat(stat_name) VALUES(:statName);";

    private final int databaseId;
    private final String name;
    private final String internalName;

    public AbstractStat(final String name, final String internalName) {
        super(GET_STAT_ID, INSERT_NEW_STAT);

        this.name = name;
        this.internalName = internalName;
        this.databaseId = this.retrieveDatabaseId();
    }

    @Override
    protected @NonNull Map<String, Object> getGetIdParameters() {
        return MapBuilder.<String, Object>ofHashMap()
                .put(STAT_NAME, this.getInternalName())
                .build();
    }

    @Override
    protected @NonNull Map<String, Object> getInsertIdParameters() {
        return MapBuilder.<String, Object>ofHashMap()
                .put(STAT_NAME, this.getInternalName())
                .build();
    }

    protected void increaseStat(final UserDb userDb) {
        this.increaseStat(userDb, 1);
    }

    protected void increaseStat(final UserDb userDb, final int value) {
        userDb.increaseStat(this, value);
    }
}
