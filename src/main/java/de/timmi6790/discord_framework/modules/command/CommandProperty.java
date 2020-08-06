package de.timmi6790.discord_framework.modules.command;

public abstract class CommandProperty<T> {
    public abstract T getValue();

    public boolean onPermissionCheck(final AbstractCommand<?> command, final CommandParameters commandParameters) {
        return true;
    }

    public boolean onCommandExecution(final AbstractCommand<?> command, final CommandParameters commandParameters) {
        return true;
    }
}
