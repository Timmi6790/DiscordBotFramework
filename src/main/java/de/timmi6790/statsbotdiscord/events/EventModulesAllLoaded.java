package de.timmi6790.statsbotdiscord.events;

import de.timmi6790.statsbotdiscord.StatsBot;
import net.dv8tion.jda.api.events.Event;

public class EventModulesAllLoaded extends Event {
    public EventModulesAllLoaded() {
        super(StatsBot.getDiscord());
    }
}
