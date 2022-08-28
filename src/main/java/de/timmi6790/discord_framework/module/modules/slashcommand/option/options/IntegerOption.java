package de.timmi6790.discord_framework.module.modules.slashcommand.option.options;

import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.DiscordOption;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Optional;

public class IntegerOption extends Option<Integer> {
    private Integer min = null;
    private Integer max = null;

    public IntegerOption(final String name, final String description) {
        super(name, description, OptionType.INTEGER);
    }

    @Override
    public String convertToOption(final Integer option) {
        return String.valueOf(option);
    }

    @Override
    public Optional<Integer> convertValue(final DiscordOption mapping) {
        return Optional.of(mapping.getAsInt());
    }

    @Override
    public OptionData build() {
        final OptionData optionData = super.build();

        if (this.min != null) {
            optionData.setMinValue(this.min);
        }

        if (this.max != null) {
            optionData.setMaxValue(this.max);
        }

        return optionData;
    }

    public IntegerOption setMax(final int max) {
        this.max = max;

        return this;
    }

    public IntegerOption setMin(final int min) {
        this.min = min;

        return this;
    }
}
