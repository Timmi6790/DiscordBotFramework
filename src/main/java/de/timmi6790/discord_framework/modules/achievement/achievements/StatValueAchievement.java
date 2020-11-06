package de.timmi6790.discord_framework.modules.achievement.achievements;

import de.timmi6790.discord_framework.modules.achievement.AbstractAchievement;
import de.timmi6790.discord_framework.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.modules.stat.AbstractStat;
import de.timmi6790.discord_framework.modules.stat.events.StatsChangeEvent;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.StringJoiner;

@EqualsAndHashCode(callSuper = true)
public abstract class StatValueAchievement extends AbstractAchievement {
    private final Class<? extends AbstractStat> stat;
    private final long requiredValue;

    protected StatValueAchievement(final String name,
                                   final Class<? extends AbstractStat> stat,
                                   final long value) {
        super(name);

        this.stat = stat;
        this.requiredValue = value;
    }

    @SubscribeEvent
    public void onStatValueChange(final StatsChangeEvent event) {
        if (!event.getStat().getClass().isAssignableFrom(this.stat) || event.getUserDb().hasAchievement(this)) {
            return;
        }

        if (event.getNewValue() >= this.requiredValue) {
            this.unlockPlayerAchievement(event.getUserDb());
            final User user = event.getUserDb().getUser();

            final StringJoiner perks = new StringJoiner("\n");
            for (final String unlocked : this.getUnlockedPerks()) {
                perks.add("- " + unlocked);
            }

            DiscordMessagesUtilities.sendPrivateMessage(user, DiscordMessagesUtilities.getEmbedBuilder(user, null)
                    .setTitle("Achievement Unlocked")
                    .setDescription(
                            "Unlocked: %s%n%nPerks:%n%s",
                            MarkdownUtil.bold(this.getName()),
                            perks.toString()
                    )
            );
        }
    }
}
