package de.timmi6790.discord_framework.exceptions;

public class ModuleGetException extends RuntimeException {
    public ModuleGetException(final String module) {
        super("Module " + module + " is not started.");
    }
}
