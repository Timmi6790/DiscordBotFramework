package de.timmi6790.discord_framework.module.modules.slashcommand.events;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CommandExecutionEvent extends Event {
    private final SlashCommand command;
    private final SlashCommandParameters parameters;

    protected CommandExecutionEvent(final JDA api,
                                    final SlashCommand command,
                                    final SlashCommandParameters commandParameters) {
        super(api);

        this.command = command;
        this.parameters = commandParameters;
    }
}
