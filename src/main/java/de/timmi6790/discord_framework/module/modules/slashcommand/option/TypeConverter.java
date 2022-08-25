package de.timmi6790.discord_framework.module.modules.slashcommand.option;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Optional;

public interface TypeConverter<T> {
    OptionType getOptionType();

    Optional<T> convertValue(OptionMapping mapping);

    T convertValueThrow(OptionMapping mapping);

    T convertValueOrDefault(OptionMapping mapping, T defaultValue);
}
