package de.timmi6790.discord_framework.module.modules.dsgvo.events;

import de.timmi6790.discord_framework.module.modules.user.UserDb;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;

/**
 * User data delete event.
 */
@Getter
public class UserDataDeleteEvent extends Event {
    /**
     * The User db.
     */
    private final UserDb userDb;

    /**
     * Instantiates a new User data delete event.
     *
     * @param jda    the jda
     * @param userDb the user db
     */
    public UserDataDeleteEvent(@NotNull final JDA jda, final UserDb userDb) {
        super(jda);

        this.userDb = userDb;
    }
}
