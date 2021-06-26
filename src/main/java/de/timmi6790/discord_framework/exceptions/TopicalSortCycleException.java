package de.timmi6790.discord_framework.exceptions;

import java.io.Serial;

public class TopicalSortCycleException extends Exception {
    @Serial
    private static final long serialVersionUID = 8382457320073252593L;

    public TopicalSortCycleException() {
        super("Cycle detected");
    }
}
