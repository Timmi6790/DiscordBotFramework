package de.timmi6790.discord_framework.modules.event.events;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.events.Event;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CommandExecutionEvent extends Event {
    private final AbstractCommand command;
    private final CommandParameters parameters;

    public CommandExecutionEvent(final AbstractCommand command, final CommandParameters parameters) {
        super(DiscordBot.getDiscord());

        this.command = command;
        this.parameters = parameters;
    }

    public static class Pre extends CommandExecutionEvent {
        public Pre(final AbstractCommand command, final CommandParameters parameters) {
            super(command, parameters);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    public static class Post extends CommandExecutionEvent {
        @Getter
        private final CommandResult commandResult;

        public Post(final AbstractCommand command, final CommandParameters parameters, final CommandResult commandResult) {
            super(command, parameters);

            this.commandResult = commandResult;
        }
    }
}
