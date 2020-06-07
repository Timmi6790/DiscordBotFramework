package de.timmi6790.statsbotdiscord.events;

import de.timmi6790.statsbotdiscord.StatsBot;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.events.Event;

@EqualsAndHashCode(callSuper = true)
@Getter
public class ModulesAllLoadedEvent extends Event {
    public ModulesAllLoadedEvent() {
        super(StatsBot.getDiscord());
    }
}
