package de.timmi6790.discord_framework.modules.new_module_manager;

import de.timmi6790.discord_framework.DiscordBot;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.Optional;


public interface Module {
    String getName();

    String getVersion();

    String[] getAuthors();

    default GatewayIntent[] getGatewayIntents() {
        return new GatewayIntent[0];
    }

    default void onDiscordReady(final ShardManager shardManager) {

    }

    default void onDisable() {

    }

    /**
     * Generate internal name string.
     *
     * @param module       the module
     * @param categoryName the category name
     * @param valueName    the value name
     * @return the string
     */
    default String generateInternalName(final Module module,
                                        final String categoryName,
                                        final String valueName) {
        return String.format("%s.%s.%s", module.getName(), categoryName, valueName)
                .replace(' ', '_')
                .toLowerCase();
    }

    default <T> Optional<T> getModule(final Class<T> clazz) {
        return DiscordBot.getInstance().getModuleManager().getModule(clazz);
    }
}
