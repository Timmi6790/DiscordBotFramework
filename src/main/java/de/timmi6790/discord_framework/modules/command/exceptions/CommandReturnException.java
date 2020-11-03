package de.timmi6790.discord_framework.modules.command.exceptions;

import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommandReturnException extends RuntimeException {
    private static final long serialVersionUID = -7239128594396319097L;
    private final MultiEmbedBuilder embedBuilder;
    private final CommandResult commandResult;

    public CommandReturnException(final MultiEmbedBuilder embedBuilder, @NonNull final CommandResult commandResult) {
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

    public CommandReturnException(@NonNull final CommandResult commandResult) {
        super("");

        this.embedBuilder = null;
        this.commandResult = commandResult;
    }

    public Optional<MultiEmbedBuilder> getEmbedBuilder() {
        return Optional.ofNullable(this.embedBuilder);
    }
}
