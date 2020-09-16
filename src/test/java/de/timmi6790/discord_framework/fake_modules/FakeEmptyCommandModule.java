package de.timmi6790.discord_framework.fake_modules;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import lombok.NonNull;

public class FakeEmptyCommandModule extends CommandModule {
    @Override
    public void registerCommands(@NonNull final AbstractModule module, final AbstractCommand<?>... commands) {
    }
}
