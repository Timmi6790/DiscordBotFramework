package de.timmi6790.discord_framework.module.modules.slashcommand.listeners;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandCause;
import de.timmi6790.discord_framework.module.modules.event.SubscribeEvent;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.CompletableFuture;

public class SlashListener {
    private final SlashCommandModule commandModule;
    private final UserDbModule userDbModule;
    private final ChannelDbModule channelDbModule;

    public SlashListener(final SlashCommandModule commandModule,
                         final UserDbModule userDbModule,
                         final ChannelDbModule channelDbModule) {
        this.commandModule = commandModule;
        this.userDbModule = userDbModule;
        this.channelDbModule = channelDbModule;
    }

    @SneakyThrows
    @SubscribeEvent
    public void onTextMessage(final SlashCommandInteractionEvent event) {
        // Add user to cache
        final User author = event.getUser();
        this.userDbModule.addUserToCache(author);

        // Get repository objects async
        final CompletableFuture<UserDb> userDbFuture = CompletableFuture.supplyAsync(() ->
                this.userDbModule.getOrCreate(author.getIdLong())
        );

        final CompletableFuture<ChannelDb> channelDbFuture = CompletableFuture.supplyAsync(() -> {
                    if (event.isFromGuild()) {
                        return this.channelDbModule.getOrCreate(
                                event.getChannel().getIdLong(),
                                event.getGuild().getIdLong()
                        );
                    } else {
                        return this.channelDbModule.getOrCreatePrivateMessage(
                                event.getChannel().getIdLong()
                        );
                    }
                }
        );

        final SlashCommandParameters commandParameters = new SlashCommandParameters(
                event,
                BaseCommandCause.MESSAGE,
                this.commandModule,
                channelDbFuture.get(),
                userDbFuture.get()
        );

        this.commandModule.getCommand(event.getName())
                .ifPresent(slashCommand -> slashCommand.executeCommand(commandParameters));
    }
}
