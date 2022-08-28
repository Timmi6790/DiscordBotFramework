package de.timmi6790.discord_framework.module.modules.slashcommand.option.options;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.DiscordOption;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Optional;

public class CommandOption extends Option<SlashCommand> {
    private final SlashCommandModule module;

    public CommandOption(final SlashCommandModule module, final String name, final String description) {
        super(name, description, OptionType.STRING);

        this.module = module;
    }

    @Override
    public String convertToOption(final SlashCommand option) {
        return option.getName();
    }

    @Override
    public Optional<SlashCommand> convertValue(final DiscordOption mapping) {
        return this.module.getCommand(mapping.getAsString());
    }
}
