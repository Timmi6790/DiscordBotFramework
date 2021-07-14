package de.timmi6790.discord_framework.module.modules.command.models;

import java.io.Serializable;

public interface CommandResult extends Serializable {
    String getExitReason();
}
