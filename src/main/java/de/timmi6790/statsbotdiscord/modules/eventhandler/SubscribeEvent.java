package de.timmi6790.statsbotdiscord.modules.eventhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscribeEvent {
    boolean async() default false;

    EventPriority priority() default EventPriority.NORMAL;
}
