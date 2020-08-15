package de.timmi6790.discord_framework.modules.command.exceptions;

import de.timmi6790.discord_framework.modules.command.CommandResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Getter
@Data
public class CommandReturnException extends RuntimeException {
    private final EmbedBuilder embedBuilder;
    private final CommandResult commandResult;

    public CommandReturnException(final EmbedBuilder embedBuilder, final CommandResult commandResult) {
        super("");

        this.embedBuilder = embedBuilder;
        this.commandResult = commandResult;
    }

    public CommandReturnException(final EmbedBuilder embedBuilder) {
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

    public Optional<EmbedBuilder> getEmbedBuilder() {
        return Optional.ofNullable(this.embedBuilder);
    }
}
