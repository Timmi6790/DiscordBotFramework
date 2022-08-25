package de.timmi6790.discord_framework.module.modules.slashcommand;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.commands.HelpSlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.listeners.MetricListener;
import de.timmi6790.discord_framework.module.modules.slashcommand.listeners.SlashListener;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class SlashCommandModule extends AbstractModule {
    private final Map<String, SlashCommand> commands = new HashMap<>();

    private Config config;

    @Getter(AccessLevel.PUBLIC)
    private PermissionsModule permissionsModule;
    @Getter(AccessLevel.PUBLIC)
    private EventModule eventModule;
    private MetricModule metricModule;

    public SlashCommandModule() {
        super("SlashCommand");

        this.addDependenciesAndLoadAfter(
                EventModule.class,
                ConfigModule.class,
                PermissionsModule.class
        );

        this.addDependencies(
                UserDbModule.class,
                ChannelDbModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.permissionsModule = this.getModuleOrThrow(PermissionsModule.class);
        this.eventModule = this.getModuleOrThrow(EventModule.class);
        this.metricModule = this.getModule(MetricModule.class).orElse(null);

        this.config = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());

        if (this.metricModule != null) {
            this.eventModule.addEventListener(
                    new MetricListener(
                            this.metricModule
                    )
            );
        }

        return true;
    }

    @Override
    public boolean onEnable() {
        if (this.config.isSetDiscordActivity()) {
            this.getDiscord().setActivity(Activity.playing("/help"));
        }

        this.registerCommands(
                new HelpSlashCommand(this)
        );

        // We currently need to register this during on enable, because otherwise we  have topical sort cycle.
        // This should be resolved with the dpi system
        this.getModuleOrThrow(EventModule.class).addEventListeners(
                new SlashListener(
                        this,
                        this.getModuleOrThrow(UserDbModule.class),
                        this.getModuleOrThrow(ChannelDbModule.class)
                )
        );

        final CommandListUpdateAction updateAction = this.getDiscord().getGuildById(757320630902194196L).updateCommands();

        final Set<String> seen = new HashSet<>();
        for (final SlashCommand slashCommand : this.commands.values()) {
            // TODO: Temporary duplicate check
            if (!seen.add(slashCommand.getName())) {
                continue;
            }

            if (slashCommand.isRequiresPermission()) {
                continue;
            }

            final SlashCommandData slashCommandData = Commands.slash(slashCommand.getName(), slashCommand.getDescription());

            final List<OptionData> options = new ArrayList<>(slashCommand.getOptions().size());
            System.out.println("Build options: " + slashCommand.getOptions().size());
            for (final Option<?> option : slashCommand.getOptions()) {
                System.out.println("Add: " + option.build());
                options.add(option.build());
            }
            slashCommandData.addOptions(options);

            updateAction.addCommands(slashCommandData);
        }
        updateAction.queue();
        return true;
    }

    public void registerCommands(final SlashCommand... commands) {
        for (final SlashCommand command : commands) {
            this.commands.put(command.getName(), command);
            for (final String alias : command.getAliases()) {
                this.commands.put(alias, command);
            }
        }
    }

    public Optional<SlashCommand> getCommand(final String commandName) {
        return Optional.ofNullable(this.commands.get(commandName));
    }

    public Map<String, SlashCommand> getCommands() {
        System.out.println("Commands: " + this.commands.size());
        return this.commands;
    }
}
