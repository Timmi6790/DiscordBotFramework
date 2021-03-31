package de.timmi6790.discord_framework.module.modules.event;

import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.Method;

@Data
class EventObject {
    @NonNull
    private final Object object;
    @NonNull
    private final Method method;
    private final boolean ignoreCanceled;
}
