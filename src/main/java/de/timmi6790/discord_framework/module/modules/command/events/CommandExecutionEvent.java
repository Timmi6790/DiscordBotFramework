package de.timmi6790.discord_framework.module.modules.command.events;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CommandExecutionEvent extends Event {
    private final Command command;
    private final CommandParameters parameters;

    protected CommandExecutionEvent(final JDA api,
                                    final Command command,
                                    final CommandParameters commandParameters) {
        super(api);

        this.command = command;
        this.parameters = commandParameters;
    }
}
