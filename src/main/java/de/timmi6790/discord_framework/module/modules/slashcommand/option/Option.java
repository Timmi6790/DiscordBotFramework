package de.timmi6790.discord_framework.module.modules.slashcommand.option;

import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.*;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter
@Setter
public abstract class Option<T> implements TypeConverter<T> {
    private final String name;
    private final String description;
    private final OptionType discordType;
    private final List<String> options = new ArrayList<>();
    private boolean required = true;

    public abstract String convertToOption(T option);

    @Override
    public abstract Optional<T> convertValue(OptionMapping mapping);

    public Option<T> addOptions(final String... options) {
        System.out.println("Add: " + Arrays.toString(options));
        this.options.addAll(List.of(options));
        return this;
    }

    public Option<T> addTypeOptions(final T... options) {
        return this.addTypeOptions(Arrays.asList(options));
    }

    public Option<T> addTypeOptions(final Collection<T> options) {
        System.out.println("Add types: " + options.size());
        for (final T option : options) {
            final String convertedOption = this.convertToOption(option);
            this.addOptions(convertedOption);
        }
        return this;
    }

    @Override
    public OptionType getOptionType() {
        return this.discordType;
    }

    @Override
    public T convertValueThrow(final OptionMapping mapping) {
        final Optional<T> optionOpt = this.convertValue(mapping);
        if (optionOpt.isPresent()) {
            return optionOpt.get();
        }

        // Send error message
        throw new CommandReturnException();
    }

    @Override
    public T convertValueOrDefault(final OptionMapping mapping, final T defaultValue) {
        return this.convertValue(mapping).orElse(defaultValue);
    }

    public T convertValueOrDefault(final OptionMapping mapping, final Supplier<T> defaultValueSupplier) {
        return this.convertValue(mapping).orElseGet(defaultValueSupplier);
    }

    public OptionData build() {
        final OptionData optionData = new OptionData(this.discordType, this.name, this.description, this.required);
        // TODO: Does not work with other natives types than string
        System.out.println("Options: " + this.options);
        for (final String option : this.options) {
            System.out.println("Adddddddddddddddddddddddddddddddddddddddd: " + this.name + " " + option);
            optionData.addChoice(option, option);
        }

        return optionData;
    }
}
