package de.timmi6790.discord_framework.module.modules.slashcommand.option.options;

import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Optional;

public class IntegerOption extends Option<Integer> {
    public IntegerOption(final String name, final String description) {
        super(name, description, OptionType.INTEGER);
    }

    @Override
    public String convertToOption(final Integer option) {
        return String.valueOf(option);
    }

    @Override
    public Optional<Integer> convertValue(final OptionMapping mapping) {
        return Optional.of(mapping.getAsInt());
    }
}
