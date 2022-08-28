package de.timmi6790.discord_framework.module.modules.slashcommand;

import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public abstract class SlashCommandGroup extends SlashCommand {
    private final Map<String, SlashCommand> commands = new HashMap<>();

    public SlashCommandGroup(final SlashCommandModule module, final String name, final String description) {
        super(module, name, description);
    }

    protected void addSubcommands(final SlashCommand... subcommands) {
        for (final SlashCommand subcommand : subcommands) {
            this.commands.put(subcommand.getName().toLowerCase(Locale.ENGLISH), subcommand);
        }
    }

    @Override
    protected CommandResult onCommand(final SlashCommandParameters parameters) {
        return this.getSubcommand(parameters.getSubCommandName().orElse(null))
                .map(command -> command.onCommand(parameters))
                .orElse(BaseCommandResult.INVALID_ARGS);
    }

    public Map<String, SlashCommand> getCommands() {
        return this.commands;
    }

    public Optional<SlashCommand> getSubcommand(final String name) {
        if (name == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.commands.get(name.toLowerCase(Locale.ENGLISH)));
    }
}
