package de.timmi6790.statsbotdiscord.exceptions;

import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Getter
@Data
public class CommandReturnException extends RuntimeException {
    private final Optional<EmbedBuilder> embedBuilder;
    private final CommandResult commandResult;

    public CommandReturnException(final EmbedBuilder embedBuilder, final CommandResult commandResult) {
        super("");

        this.embedBuilder = Optional.ofNullable(embedBuilder);
        this.commandResult = commandResult;
    }

    public CommandReturnException(final EmbedBuilder embedBuilder) {
        super("");

        this.embedBuilder = Optional.ofNullable(embedBuilder);
        this.commandResult = CommandResult.INVALID_ARGS;
    }

    public CommandReturnException() {
        super("");

        this.embedBuilder = Optional.empty();
        this.commandResult = CommandResult.INVALID_ARGS;
    }
}
