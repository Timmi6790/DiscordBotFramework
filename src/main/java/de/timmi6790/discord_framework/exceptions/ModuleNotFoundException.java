package de.timmi6790.discord_framework.exceptions;

public class ModuleNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -4584396945658349823L;

    public ModuleNotFoundException(final String module) {
        super("Module " + module + " is not started.");
    }
}
