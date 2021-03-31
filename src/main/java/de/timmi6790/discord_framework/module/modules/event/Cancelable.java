package de.timmi6790.discord_framework.module.modules.event;

public interface Cancelable {
    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
