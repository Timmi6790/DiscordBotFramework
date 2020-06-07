package de.timmi6790.statsbotdiscord.events;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import de.timmi6790.statsbotdiscord.modules.stat.AbstractStat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.events.Event;

@EqualsAndHashCode(callSuper = true)
@Getter
public class StatsChangeEvent extends Event {
    private final UserDb userDb;
    private final AbstractStat stat;
    private final long oldValue;
    private final long newValue;

    public StatsChangeEvent(final UserDb userDb, final AbstractStat stat, final long oldValue, final long newValue) {
        super(StatsBot.getDiscord());

        this.userDb = userDb;
        this.stat = stat;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
