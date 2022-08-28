package de.timmi6790.discord_framework.module.modules.slashcommand.option;

import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.DiscordOption;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Optional;

public interface TypeConverter<T> {
    OptionType getOptionType();

    Optional<T> convertValue(DiscordOption mapping);

    T convertValueThrow(DiscordOption mapping);

    T convertValueOrDefault(DiscordOption mapping, T defaultValue);
}
