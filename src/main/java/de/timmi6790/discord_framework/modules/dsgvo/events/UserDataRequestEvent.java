package de.timmi6790.discord_framework.modules.dsgvo.events;

import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * User data request event.
 */
@Getter
public class UserDataRequestEvent extends Event {
    /**
     * The User db.
     */
    private final UserDb userDb;
    /**
     * The user data.
     */
    private final Map<Object, Object> dataMap = new HashMap<>();

    /**
     * Instantiates a new User data request event.
     *
     * @param jda    the jda instance
     * @param userDb the user db
     */
    public UserDataRequestEvent(@NotNull final JDA jda, final UserDb userDb) {
        super(jda);

        this.userDb = userDb;
    }

    /**
     * Add new user data.
     *
     * @param key   the data name
     * @param value the value
     */
    public void addData(final Object key, final Object value) {
        this.dataMap.put(key, value);
    }
}
