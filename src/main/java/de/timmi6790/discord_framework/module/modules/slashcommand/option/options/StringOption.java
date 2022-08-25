package de.timmi6790.discord_framework.module.modules.slashcommand.option.options;

import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Optional;

public class StringOption extends Option<String> {
    public StringOption(final String name, final String description) {
        super(name, description, OptionType.STRING);
    }

    @Override
    public String convertToOption(final String option) {
        return option;
    }

    @Override
    public Optional<String> convertValue(final OptionMapping mapping) {
        return Optional.of(mapping.getAsString());
    }
}
