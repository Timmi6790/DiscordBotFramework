package de.timmi6790.discord_framework.module.modules.dsgvo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.command_old.CommandModule;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.dsgvo.commands.AccountDeletionCommand;
import de.timmi6790.discord_framework.module.modules.dsgvo.commands.DataRequestCommand;
import de.timmi6790.discord_framework.module.modules.dsgvo.events.UserDataDeleteEvent;
import de.timmi6790.discord_framework.module.modules.dsgvo.events.UserDataRequestEvent;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import lombok.EqualsAndHashCode;

/**
 * DSGVO module.
 */
@EqualsAndHashCode(callSuper = true)
public class DsgvoModule extends AbstractModule {
    /**
     * The Gson.
     */
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();

    /**
     * Instantiates a new Dsgvo module.
     */
    public DsgvoModule() {
        super("DSGVO");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class,
                CommandModule.class,
                EventModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new AccountDeletionCommand(this),
                new DataRequestCommand(this)
        );
        return true;
    }

    /**
     * Gets user data. This is separated from the command to allow better unit tests
     *
     * @param userDb the user db
     * @return the user data
     */
    public String getUserData(final UserDb userDb) {
        final UserDataRequestEvent dataRequestEvent = new UserDataRequestEvent(
                this.getDiscordBot().getBaseShard(),
                userDb
        );
        this.getModuleOrThrow(EventModule.class).executeEvent(dataRequestEvent);

        return this.gson.toJson(dataRequestEvent.getDataMap());
    }

    /**
     * Delete user data. This is separated from the command to allow better unit tests
     *
     * @param userDb the user db
     */
    public void deleteUserData(final UserDb userDb) {
        final UserDataDeleteEvent dataDeleteEvent = new UserDataDeleteEvent(
                this.getDiscordBot().getBaseShard(),
                userDb
        );
        this.getModuleOrThrow(EventModule.class).executeEvent(dataDeleteEvent);
    }
}
