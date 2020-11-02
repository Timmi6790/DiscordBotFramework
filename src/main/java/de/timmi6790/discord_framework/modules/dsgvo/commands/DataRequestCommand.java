package de.timmi6790.discord_framework.modules.dsgvo.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.CooldownCommandProperty;
import de.timmi6790.discord_framework.modules.dsgvo.DsgvoModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
@Getter
public class DataRequestCommand extends AbstractCommand {
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();

    private final DsgvoModule dsgvoModule;

    public DataRequestCommand() {
        super("giveMeMyData", "Info", "Get all my data!", "");

        this.addProperties(
                new CooldownCommandProperty(1, TimeUnit.DAYS)
        );

        this.dsgvoModule = getModuleManager().getModuleOrThrow(DsgvoModule.class);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final long userId = commandParameters.getUserDb().getDiscordId();

        final String userData = this.getGson().toJson(this.getDsgvoModule().getUserData(userId));
        commandParameters.getUserTextChannel().sendFile(
                userData.getBytes(StandardCharsets.UTF_8),
                "Your-personal-data.json"
        ).queue();

        return CommandResult.SUCCESS;
    }
}
