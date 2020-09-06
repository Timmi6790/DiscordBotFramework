package de.timmi6790.discord_framework.modules.command.exceptions;

import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Getter
@Data
public class CommandReturnException extends RuntimeException {
    private final MultiEmbedBuilder embedBuilder;
    private final CommandResult commandResult;

    public CommandReturnException(final MultiEmbedBuilder embedBuilder, final CommandResult commandResult) {
        super("");

        this.embedBuilder = embedBuilder;
        this.commandResult = commandResult;
    }

    public CommandReturnException(final MultiEmbedBuilder embedBuilder) {
        super("");

        this.embedBuilder = embedBuilder;
        this.commandResult = CommandResult.INVALID_ARGS;
    }

    public CommandReturnException() {
        super("");

        this.embedBuilder = null;
        this.commandResult = CommandResult.INVALID_ARGS;
    }

    public CommandReturnException(final CommandResult commandResult) {
        super("");

        this.embedBuilder = null;
        this.commandResult = commandResult;
    }

    public Optional<MultiEmbedBuilder> getEmbedBuilder() {
        return Optional.ofNullable(this.embedBuilder);
    }
}
