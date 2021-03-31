package de.timmi6790.discord_framework.module.modules.stat.events;

import de.timmi6790.discord_framework.module.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;

@EqualsAndHashCode(callSuper = true)
@Getter
public class StatsChangeEvent extends Event {
    private final UserDb userDb;
    private final AbstractStat stat;
    private final long oldValue;
    private final long newValue;

    public StatsChangeEvent(final JDA discord,
                            final UserDb userDb,
                            final AbstractStat stat,
                            final long oldValue,
                            final long newValue) {
        super(discord);

        this.userDb = userDb;
        this.stat = stat;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
