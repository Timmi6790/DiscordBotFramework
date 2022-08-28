package de.timmi6790.discord_framework.module.modules.slashcommand.property;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;

/**
 * CommandProperty interface. Used to control settings for permission and execution checks
 *
 * @param <T> the value type of the property
 */
public interface SlashCommandProperty<T> {
    /**
     * Gets value.
     *
     * @return the value
     */
    T getValue();

    /**
     * Checks if the property is successful in the command permission check.
     *
     * @param command           the command
     * @param commandParameters the command parameters
     * @return is successful
     */
    default boolean onPermissionCheck(final SlashCommand command,
                                      final SlashCommandParameters commandParameters) {
        return true;
    }

    /**
     * Checks if the property is successful in the command execution.
     *
     * @param command           the command
     * @param commandParameters the command parameters
     * @return is successful
     */
    default boolean onCommandExecution(final SlashCommand command,
                                       final SlashCommandParameters commandParameters) {
        return true;
    }
}
