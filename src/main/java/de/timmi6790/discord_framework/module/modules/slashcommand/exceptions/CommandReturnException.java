package de.timmi6790.discord_framework.module.modules.slashcommand.exceptions;

import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CommandReturnException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -1699133334505093446L;

    private final CommandResult commandResult;

    public CommandReturnException() {
        this(BaseCommandResult.INVALID_ARGS);
    }
}
