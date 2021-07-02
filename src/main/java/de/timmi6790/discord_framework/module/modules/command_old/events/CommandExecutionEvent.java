package de.timmi6790.discord_framework.module.modules.command_old.events;

import de.timmi6790.discord_framework.module.modules.command_old.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command_old.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command_old.CommandResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.events.Event;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CommandExecutionEvent extends Event {
    private final AbstractCommand command;
    private final CommandParameters parameters;

    public CommandExecutionEvent(@NonNull final AbstractCommand command, @NonNull final CommandParameters parameters) {
        super(parameters.getJda());

        this.command = command;
        this.parameters = parameters;
    }

    public static class Pre extends CommandExecutionEvent {
        public Pre(final AbstractCommand command, final CommandParameters parameters) {
            super(command, parameters);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Getter
    public static class Post extends CommandExecutionEvent {
        private final CommandResult commandResult;

        public Post(final AbstractCommand command,
                    final CommandParameters parameters,
                    @NonNull final CommandResult commandResult) {
            super(command, parameters);

            this.commandResult = commandResult;
        }
    }
}
