package de.timmi6790.discord_framework.modules.command;

import lombok.NonNull;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.Map;
import java.util.Optional;

public class CommandGroup extends AbstractCommand {
    private final Map<String, AbstractCommand> subCommands = new CaseInsensitiveMap<>();
    private final Map<String, String> subCommandsAlias = new CaseInsensitiveMap<>();

    protected CommandGroup(@NonNull final String name,
                           @NonNull final String category,
                           @NonNull final String description,
                           @NonNull final String syntax,
                           final String... aliasNames) {
        super(name, category, description, syntax, aliasNames);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        return null;
    }

    protected boolean addSubCommand(final AbstractCommand command) {
        if (!this.hasSubCommand(command.getName())) {
            this.subCommands.put(command.getName(), command);
            for (final String aliasName : command.getAliasNames()) {
                this.subCommandsAlias.put(aliasName, command.getName());
            }
            return true;
        }
        return false;
    }

    private Optional<AbstractCommand> getSubCommand(final String commandName) {
        return Optional.ofNullable(this.subCommands.get(this.subCommandsAlias.getOrDefault(commandName, commandName)));
    }

    public boolean hasSubCommand(final String commandName) {
        return this.getSubCommand(commandName).isPresent();
    }

    public boolean hasSubCommand(final AbstractCommand command) {
        return this.subCommands.containsValue(command);
    }

}
