package de.timmi6790.discord_framework.module.exceptions;

import java.io.Serial;

public class ModuleNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -480705207437502908L;

    public ModuleNotFoundException(final String module) {
        super("Module " + module + " is not started.");
    }
}
