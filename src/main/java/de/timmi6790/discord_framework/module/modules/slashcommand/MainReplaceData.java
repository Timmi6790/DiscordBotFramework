package de.timmi6790.discord_framework.module.modules.slashcommand;

import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.DiscordOption;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class MainReplaceData {
    private final Class<? extends SlashCommand> commandClass;
    private final String subCommand;
    private final Map<String, DiscordOption> newArgs;

    public MainReplaceData(final Class<? extends SlashCommand> commandClass, final String subCommand, final List<DiscordOption> options) {
        this(
                commandClass,
                subCommand,
                SlashCommandParameters.formatOptions(options)
        );
    }
}
