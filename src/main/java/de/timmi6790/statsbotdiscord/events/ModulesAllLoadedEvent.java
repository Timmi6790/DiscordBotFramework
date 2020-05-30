package de.timmi6790.statsbotdiscord.events;

import de.timmi6790.statsbotdiscord.StatsBot;
import net.dv8tion.jda.api.events.Event;

public class ModulesAllLoadedEvent extends Event {
    public ModulesAllLoadedEvent() {
        super(StatsBot.getDiscord());
    }
}
