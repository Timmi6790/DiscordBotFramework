package de.timmi6790.discord_framework.module.modules.slashcommand.option.options;

import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.DiscordOption;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Optional;

public class EnumOption<T extends Enum<T>> extends Option<T> {
    private final Class<T> enumClass;

    public EnumOption(final Class<T> enumClass, final String name, final String description) {
        super(name, description, OptionType.STRING);

        this.enumClass = enumClass;

        this.addTypeOptions(enumClass.getEnumConstants());
    }

    @Override
    public String convertToOption(final T option) {
        return option.name();
    }

    @Override
    public Optional<T> convertValue(final DiscordOption mapping) {
        try {
            final T value = Enum.valueOf(this.enumClass, mapping.getAsString());
            return Optional.of(value);
        } catch (final Exception exception) {
            return Optional.empty();
        }
    }
}
