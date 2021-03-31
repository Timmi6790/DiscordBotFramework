package de.timmi6790.discord_framework.module.modules.command;

import lombok.Data;

@Data
public class Config {
    private String mainCommand = "stat ";
    private boolean setDiscordActivity = true;
}
