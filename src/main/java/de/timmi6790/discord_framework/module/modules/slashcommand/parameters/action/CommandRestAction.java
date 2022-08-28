package de.timmi6790.discord_framework.module.modules.slashcommand.parameters.action;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class CommandRestAction implements RestAction<Message> {
    public CommandRestAction setActionRow(@Nonnull final ItemComponent... components) {
        return this.setActionRows(components);
    }

    public CommandRestAction setActionRow(@Nonnull final Collection<? extends ItemComponent> components) {
        return this.setActionRows(components);
    }

    public CommandRestAction setActionRows(@Nonnull final Collection<? extends ItemComponent> rows) {
        return this.setActionRows(rows.toArray(new ItemComponent[0]));
    }

    public abstract CommandRestAction setActionRows(@Nonnull ItemComponent... rows);
}
