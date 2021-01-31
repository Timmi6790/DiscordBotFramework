package de.timmi6790.discord_framework.modules.dsgvo;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.dsgvo.commands.AccountDeletionCommand;
import de.timmi6790.discord_framework.modules.dsgvo.commands.DataRequestCommand;
import lombok.EqualsAndHashCode;

/**
 * DSGVO module.
 */
@EqualsAndHashCode(callSuper = true)
public class DsgvoModule extends AbstractModule {
    /**
     * Instantiates a new Dsgvo module.
     */
    public DsgvoModule() {
        super("DSGVO");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class,
                CommandModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new AccountDeletionCommand(),
                new DataRequestCommand()
        );
    }
}
