package de.timmi6790.discord_framework.module.exceptions;

public class ModuleNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -480705207437502908L;

    public ModuleNotFoundException(final String module) {
        super("Module " + module + " is not started.");
    }
}
