package de.timmi6790.discord_framework.module.modules.slashcommand.parameters.action;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class UpdateRestAction extends CommandRestAction {
    private final WebhookMessageEditAction<Message> action;

    @Override
    public JDA getJDA() {
        return this.action.getJDA();
    }

    @NotNull
    @Override
    public RestAction<Message> setCheck(@Nullable final BooleanSupplier checks) {
        return this.action.setCheck(checks);
    }

    @Override
    public void queue(@Nullable final Consumer<? super Message> success, @Nullable final Consumer<? super Throwable> failure) {
        this.action.queue(success, failure);
    }

    @Override
    public Message complete(final boolean shouldQueue) throws RateLimitedException {
        return this.action.complete(shouldQueue);
    }

    @NotNull
    @Override
    public CompletableFuture<Message> submit(final boolean shouldQueue) {
        return this.action.submit(shouldQueue);
    }

    @Override
    public CommandRestAction setActionRows(@NotNull final ItemComponent... items) {
        this.action.setActionRow(items);
        return this;
    }
}
