package de.timmi6790.discord_framework.modules.dsgvo;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.config.ConfigModule;
import de.timmi6790.discord_framework.modules.database.DatabaseModule;
import de.timmi6790.discord_framework.modules.dsgvo.commands.AccountDeletionCommand;
import de.timmi6790.discord_framework.modules.dsgvo.commands.DataRequestCommand;
import de.timmi6790.discord_framework.modules.dsgvo.repository.DsgvoRepository;
import de.timmi6790.discord_framework.modules.dsgvo.repository.DsgvoRepositoryMysql;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class DsgvoModule extends AbstractModule {
    private DsgvoRepository dsgvoRepository;

    public DsgvoModule() {
        super("DSGVO");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class,
                DatabaseModule.class,
                CommandModule.class
        );
    }

    @Override
    public void onInitialize() {
        this.dsgvoRepository = new DsgvoRepositoryMysql(this);

        this.getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new AccountDeletionCommand(),
                new DataRequestCommand()
        );
    }

    public UserData getUserData(final long discordUserId) {
        return this.dsgvoRepository.getUserData(discordUserId);
    }
}
