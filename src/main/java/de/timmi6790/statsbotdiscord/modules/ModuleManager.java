package de.timmi6790.statsbotdiscord.modules;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.events.ModulesAllLoadedEvent;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Data
public class ModuleManager {
    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);

    private final Map<Class<? extends AbstractModule>, AbstractModule> modules = new HashMap<>();

    public boolean registerModule(final AbstractModule module) {
        if (this.modules.containsKey(module.getClass())) {
            return false;
        }

        this.modules.put(module.getClass(), module);
        return true;
    }

    public void registerModules(final AbstractModule... modules) {
        Arrays.stream(modules).forEach(this::registerModule);
    }

    public AbstractModule getModule(final Class<? extends AbstractModule> clazz) {
        return this.modules.get(clazz);
    }

    public void startAll() {
        for (final AbstractModule module : this.modules.values()) {
            logger.info("Starting module {}", module.getName());
            try {
                module.onEnable();
            } catch (final Exception e) {
                logger.error(module.getName(), e);
            }
        }

        StatsBot.getEventManager().executeEvent(new ModulesAllLoadedEvent());
    }
}
