package de.timmi6790.discord_framework.modules;

import de.timmi6790.discord_framework.DiscordBot;
import lombok.SneakyThrows;

import java.lang.reflect.ParameterizedType;

public abstract class GetModule<T extends AbstractModule> {
    private Class<T> moduleClass;

    @SneakyThrows
    protected Class<T> getModuleClass() {
        if (this.moduleClass == null) {
            try {
                final ParameterizedType type;
                Class<?> clazz = this.getClass();
                // Find Lowest extending class
                while (true) {
                    if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
                        type = (ParameterizedType) clazz.getGenericSuperclass();
                        break;
                    }

                    clazz = (Class<?>) clazz.getGenericSuperclass();
                }

                final String className = type.getActualTypeArguments()[0].getTypeName();
                this.moduleClass = (Class<T>) Class.forName(className, true, this.getClass().getClassLoader());
            } catch (final Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Class is not parametrized with generic type! Please use extends<>");
            }
        }

        return this.moduleClass;
    }

    protected T getModule() {
        return this.getModuleManager().getModuleOrThrow(this.getModuleClass());
    }

    protected ModuleManager getModuleManager() {
        return DiscordBot.getInstance().getModuleManager();
    }
}
