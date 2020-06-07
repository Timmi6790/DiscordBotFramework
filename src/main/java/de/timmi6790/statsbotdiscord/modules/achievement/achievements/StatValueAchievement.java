package de.timmi6790.statsbotdiscord.modules.achievement.achievements;

import de.timmi6790.statsbotdiscord.events.StatsChangeEvent;
import de.timmi6790.statsbotdiscord.modules.achievement.AbstractAchievement;
import de.timmi6790.statsbotdiscord.modules.eventhandler.SubscribeEvent;
import de.timmi6790.statsbotdiscord.modules.stat.AbstractStat;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;

import java.util.Optional;

public abstract class StatValueAchievement extends AbstractAchievement {
    private final Class<? extends AbstractStat> stat;
    private final long requiredValue;

    public StatValueAchievement(final String name, final String internalName, final Class<? extends AbstractStat> stat, final long value) {
        super(name, internalName);

        this.stat = stat;
        this.requiredValue = value;
    }

    @SubscribeEvent
    public void onStatValueChange(final StatsChangeEvent event) {
        if (!event.getStat().getClass().isAssignableFrom(this.stat) || event.getUserDb().hasAchievement(this)) {
            return;
        }

        if (event.getNewValue() >= this.requiredValue) {
            event.getUserDb().grantAchievement(this);

            event.getUserDb().getUser().ifPresent(user -> {
                UtilitiesDiscord.sendPrivateMessage(user, UtilitiesDiscord.getDefaultEmbedBuilder(user, Optional.empty())
                        .setTitle("Achievement Unlocked")
                        .setDescription("Unlocked: " + this.getName())
                );
            });
        }
    }
}
