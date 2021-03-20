package de.timmi6790.discord_framework.modules.rank;

import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.new_module_manager.dpi.Service;
import de.timmi6790.discord_framework.modules.rank.commands.RankCommand;

@Service
public class RankCommandService {
    public RankCommandService(final RankModule rankModule, final CommandModule commandModule) {
        commandModule
                .registerCommands(
                        rankModule,
                        new RankCommand()
                );
    }
}
