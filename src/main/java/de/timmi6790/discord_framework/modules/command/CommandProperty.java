package de.timmi6790.discord_framework.modules.command;

import lombok.NonNull;

public abstract class CommandProperty<T> {
    public abstract T getValue();

    public boolean onPermissionCheck(@NonNull final AbstractCommand<?> command, @NonNull final CommandParameters commandParameters) {
        return true;
    }

    public boolean onCommandExecution(@NonNull final AbstractCommand<?> command, @NonNull final CommandParameters commandParameters) {
        return true;
    }
}
