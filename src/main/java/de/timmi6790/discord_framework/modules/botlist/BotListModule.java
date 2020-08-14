package de.timmi6790.discord_framework.modules.botlist;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import lombok.EqualsAndHashCode;
import org.discordbots.api.client.DiscordBotListAPI;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class BotListModule extends AbstractModule {
    private ScheduledFuture<?> updateTask;

    public BotListModule() {
        super("BotList");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.getModuleOrThrow(ConfigModule.class).registerAndGetConfig(this, new Config());
    }

    @Override
    public void onEnable() {
        // Bot list server count update task
        final String discordListToken = this.getModuleOrThrow(ConfigModule.class)
                .getConfig(this, Config.class)
                .getDiscordListToken();
        if (!discordListToken.isEmpty()) {
            final DiscordBotListAPI botListAPI = new DiscordBotListAPI.Builder()
                    .token(discordListToken)
                    .botId(this.getDiscord().getSelfUser().getId())
                    .build();

            this.updateTask = Executors.newScheduledThreadPool(1)
                    .scheduleAtFixedRate(
                            () -> botListAPI.setStats(this.getDiscord().getGuilds().size()),
                            0,
                            30,
                            TimeUnit.MINUTES
                    );
        }
    }

    @Override
    public void onDisable() {
        this.updateTask.cancel(true);
    }
}
