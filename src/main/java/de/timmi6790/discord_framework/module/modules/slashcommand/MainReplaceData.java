package de.timmi6790.discord_framework.module.modules.slashcommand;

import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class MainReplaceData {
    private final Class<? extends SlashCommand> commandClass;
    private final String subCommand;
    private final Map<String, OptionMapping> newArgs;

    public MainReplaceData(final Class<? extends SlashCommand> commandClass, final String subCommand, final List<OptionMapping> options) {
        this(
                commandClass,
                subCommand,
                SlashCommandParameters.formatOptions(options)
        );
    }
}
