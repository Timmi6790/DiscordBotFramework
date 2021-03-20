package de.timmi6790.discord_framework.modules.dsgvo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.dsgvo.commands.AccountDeletionCommand;
import de.timmi6790.discord_framework.modules.dsgvo.commands.DataRequestCommand;
import de.timmi6790.discord_framework.modules.dsgvo.events.UserDataDeleteEvent;
import de.timmi6790.discord_framework.modules.dsgvo.events.UserDataRequestEvent;
import de.timmi6790.discord_framework.modules.event.EventModule;
import de.timmi6790.discord_framework.modules.new_module_manager.Module;
import de.timmi6790.discord_framework.modules.user.UserDb;
import lombok.EqualsAndHashCode;

/**
 * DSGVO module.
 */
@EqualsAndHashCode
public class DsgvoModule implements Module {
    /**
     * The Gson.
     */
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();

    private final DiscordBot discordBot;
    private final EventModule eventModule;

    /**
     * Instantiates a new Dsgvo module.
     */
    public DsgvoModule(final DiscordBot discordBot,
                       final CommandModule commandModule,
                       final EventModule eventModule) {
        this.discordBot = discordBot;
        this.eventModule = eventModule;

        commandModule.registerCommands(
                this,
                new AccountDeletionCommand(this),
                new DataRequestCommand(this)
        );
    }

    @Override
    public String getName() {
        return "DSGVO";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getAuthors() {
        return new String[]{"Timmi6790"};
    }

    /**
     * Gets user data. This is separated from the command to allow better unit tests
     *
     * @param userDb the user db
     * @return the user data
     */
    public String getUserData(final UserDb userDb) {
        final UserDataRequestEvent dataRequestEvent = new UserDataRequestEvent(
                this.discordBot.getBaseShard(),
                userDb
        );
        this.eventModule.executeEvent(dataRequestEvent);

        return this.gson.toJson(dataRequestEvent.getDataMap());
    }

    /**
     * Delete user data. This is separated from the command to allow better unit tests
     *
     * @param userDb the user db
     */
    public void deleteUserData(final UserDb userDb) {
        final UserDataDeleteEvent dataDeleteEvent = new UserDataDeleteEvent(
                this.discordBot.getBaseShard(),
                userDb
        );
        this.eventModule.executeEvent(dataDeleteEvent);
    }
}
