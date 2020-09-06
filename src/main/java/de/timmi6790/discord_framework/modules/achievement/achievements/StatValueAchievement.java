package de.timmi6790.discord_framework.modules.achievement.achievements;

import de.timmi6790.discord_framework.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.modules.stat.events.StatsChangeEvent;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.User;

@EqualsAndHashCode(callSuper = true)
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

            final User user = event.getUserDb().getUser();
            DiscordMessagesUtilities.sendPrivateMessage(user, DiscordMessagesUtilities.getEmbedBuilder(user, null)
                    .setTitle("Achievement Unlocked")
                    .setDescription("Unlocked: " + this.getName())
            );
        }
    }
}
