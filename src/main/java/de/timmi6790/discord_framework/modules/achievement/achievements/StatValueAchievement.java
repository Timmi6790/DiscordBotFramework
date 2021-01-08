package de.timmi6790.discord_framework.modules.achievement.achievements;

import de.timmi6790.discord_framework.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.modules.stat.events.StatsChangeEvent;
import lombok.EqualsAndHashCode;

/**
 * Stat achievement that is triggers when a user reaches a specific stat value
 */
@EqualsAndHashCode(callSuper = true)
public abstract class StatValueAchievement extends AbstractAchievement {
    private final Class<? extends AbstractStat> stat;
    private final long requiredValue;

    /**
     * Instantiates a new Stat value achievement.
     *
     * @param achievementName the achievement name
     * @param statClass       the statClass
     * @param requiredValue   the required stat value for the achivement
     */
    protected StatValueAchievement(final String achievementName,
                                   final Class<? extends AbstractStat> statClass,
                                   final long requiredValue) {
        super(achievementName);

        this.stat = statClass;
        this.requiredValue = requiredValue;
    }

    /**
     * On stat value change.
     *
     * @param event the event
     */
    @SubscribeEvent
    public void onStatValueChange(final StatsChangeEvent event) {
        if (!event.getStat().getClass().isAssignableFrom(this.stat) || event.getUserDb().hasAchievement(this)) {
            return;
        }

        if (event.getNewValue() >= this.requiredValue) {
            this.unlockPlayerAchievement(event.getUserDb(), true);
        }
    }
}
