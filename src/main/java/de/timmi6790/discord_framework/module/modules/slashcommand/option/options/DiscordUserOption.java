package de.timmi6790.discord_framework.module.modules.slashcommand.option.options;

import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Optional;

public class DiscordUserOption extends Option<User> {
    public DiscordUserOption(final String name, final String description) {
        super(name, description, OptionType.USER);
    }

    @Override
    public String convertToOption(final User option) {
        return option.getName();
    }

    @Override
    public Optional<User> convertValue(final OptionMapping mapping) {
        return Optional.of(mapping.getAsUser());
    }
}
