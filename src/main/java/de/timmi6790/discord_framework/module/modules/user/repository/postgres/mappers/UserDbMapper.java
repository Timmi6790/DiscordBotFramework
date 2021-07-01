package de.timmi6790.discord_framework.module.modules.user.repository.postgres.mappers;

import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class UserDbMapper implements RowMapper<UserDb> {
    private final UserDbModule userDbModule;
    private final EventModule eventModule;
    private final RankModule rankModule;
    private final @Nullable SettingModule settingModule;

    @Override
    public UserDb map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        final int mainRankId = rs.getInt("primary_rank_id");
        return new UserDb(
                rs.getLong("discord_id"),
                this.rankModule.getRank(mainRankId).orElseGet(this.rankModule::getDefaultRank),
                rs.getBoolean("banned"),
                this.userDbModule,
                this.eventModule,
                this.settingModule
        );
    }
}