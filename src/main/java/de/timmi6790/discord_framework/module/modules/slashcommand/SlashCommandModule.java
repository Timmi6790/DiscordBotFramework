package de.timmi6790.discord_framework.module.modules.slashcommand;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReaction;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReactionModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.CommandButtonAction;
import de.timmi6790.discord_framework.module.modules.slashcommand.commands.HelpSlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.listeners.MetricListener;
import de.timmi6790.discord_framework.module.modules.slashcommand.listeners.SlashListener;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.StoredSlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.controll.AllowPrivateMessageProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.controll.RequiredDiscordUserPermsProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.commons.StringUtilities;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

@EqualsAndHashCode(callSuper = true)
@Log4j2
public class SlashCommandModule extends AbstractModule {
    private final Map<String, SlashCommand> commands = new HashMap<>();

    private Config config;

    @Getter(AccessLevel.PUBLIC)
    private PermissionsModule permissionsModule;
    @Getter(AccessLevel.PUBLIC)
    private EventModule eventModule;
    private MetricModule metricModule;
    private ButtonReactionModule buttonReactionModule;

    public SlashCommandModule() {
        super("Command");

        this.addDiscordGatewayIntents(
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.GUILD_MESSAGES
        );

        this.addDependenciesAndLoadAfter(
                EventModule.class,
                ConfigModule.class,
                PermissionsModule.class,
                ButtonReactionModule.class
        );

        this.addDependencies(
                UserDbModule.class,
                ChannelDbModule.class
        );
    }

    protected String getCommandPermissionNode(final AbstractModule module, final SlashCommand command) {
        return String.format(
                        "%s.command.%s",
                        module.getModuleName(),
                        command.getName()
                )
                .replace(' ', '_')
                .toLowerCase();
    }

    @Override
    public boolean onInitialize() {
        this.permissionsModule = this.getModuleOrThrow(PermissionsModule.class);
        this.eventModule = this.getModuleOrThrow(EventModule.class);
        this.metricModule = this.getModule(MetricModule.class).orElse(null);
        this.buttonReactionModule = this.getModuleOrThrow(ButtonReactionModule.class);

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
                this,
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

            final SlashCommandData slashCommandData = Commands.slash(slashCommand.getName().toLowerCase(Locale.ENGLISH), slashCommand.getDescription().isEmpty() ? "TEST COMMAND" : slashCommand.getDescription());

            final List<Option<?>> slashOptions = new ArrayList<>(slashCommand.getOptions());
            slashOptions.sort(new OptionComparator(slashOptions));

            final List<OptionData> options = new ArrayList<>(slashOptions.size());
            for (final Option<?> option : slashOptions) {
                options.add(option.build());
            }
            slashCommandData.addOptions(options);

            final boolean allowPrivateOpt = slashCommand.getPropertyValueOrDefault(AllowPrivateMessageProperty.class, () -> true);
            if (!allowPrivateOpt) {
                slashCommandData.setGuildOnly(true);
            }

            final EnumSet<Permission> requiredPermissions = slashCommand.getPropertyValueOrDefault(RequiredDiscordUserPermsProperty.class, () -> EnumSet.noneOf(Permission.class));
            if (!requiredPermissions.isEmpty()) {
                slashCommandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(requiredPermissions));
            }

            // Sub commands
            if (slashCommand instanceof final SlashCommandGroup slashCommandGroup) {
                final List<SubcommandData> subcommands = new ArrayList<>(slashCommandGroup.getCommands().size());
                for (final SlashCommand subCommand : slashCommandGroup.getCommands().values()) {
                    final SubcommandData subcommandData = new SubcommandData(subCommand.getName(), subCommand.getDescription());

                    final List<Option<?>> subSlashOptions = new ArrayList<>(subCommand.getOptions());
                    subSlashOptions.sort(new OptionComparator(subSlashOptions));

                    final List<OptionData> subOptions = new ArrayList<>(subSlashOptions.size());
                    for (final Option<?> option : subSlashOptions) {
                        subOptions.add(option.build());
                    }
                    subcommandData.addOptions(subOptions);

                    subcommands.add(subcommandData);
                }

                if (!subcommands.isEmpty()) {
                    slashCommandData.addSubcommands(subcommands);
                }
            }

            updateAction.addCommands(slashCommandData);
        }
        updateAction.queue();
        return true;
    }

    public void registerCommands(final AbstractModule module, final SlashCommand... commands) {
        for (final SlashCommand command : commands) {
            this.registerCommand(module, command);
        }
    }

    public void registerCommand(final AbstractModule module, final SlashCommand command) {
        if (this.commands.containsKey(command.getName())) {
            log.warn(
                    "The module {} tried to register the {} command that already exists.",
                    module.getModuleName(),
                    command.getName()
            );
            return;
        }

        // Only set the permission id when it is the default one
        if (command.hasDefaultPermission()) {
            final String permissionNode = this.getCommandPermissionNode(module, command);
            final int permissionId = this.getPermissionsModule().addPermission(permissionNode);
            command.setPermissionId(permissionId);
        }

        log.info(
                "[{}] Registered {} command",
                module.getModuleName(),
                command.getName()
        );

        this.commands.put(command.getName().toLowerCase(Locale.ENGLISH), command);
        for (final String aliasName : command.getPropertyValueOrDefault(AliasNamesProperty.class, () -> new String[0])) {
            final SlashCommand existingAliasName = this.commands.get(aliasName.toLowerCase(Locale.ENGLISH));
            if (existingAliasName == null) {
                this.commands.put(aliasName.toLowerCase(Locale.ENGLISH), command);
            } else {
                log.warn(
                        "[{}] Tried to register an already existing alias name {} for {} that is already used for the {} command",
                        module.getModuleName(),
                        aliasName,
                        command.getName(),
                        existingAliasName
                );
            }
        }
    }

    public Optional<SlashCommand> getCommand(final String commandName) {
        return Optional.ofNullable(this.commands.get(commandName.toLowerCase(Locale.ENGLISH)));
    }

    public Optional<SlashCommand> getCommand(final Class<? extends SlashCommand> slashCommandClass) {
        return this.getCommand(command -> command.getClass().equals(slashCommandClass));
    }

    public Optional<SlashCommand> getCommand(final Predicate<SlashCommand> commandPredicate) {
        for (final SlashCommand command : this.commands.values()) {
            if (commandPredicate.test(command)) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }

    public Map<String, SlashCommand> getCommands() {
        return this.commands;
    }

    public Set<SlashCommand> getCommands(final Predicate<SlashCommand> commandPredicate) {
        final Set<SlashCommand> filteredCommands = new HashSet<>();

        for (final SlashCommand command : this.commands.values()) {
            if (commandPredicate.test(command)) {
                filteredCommands.add(command);
            }
        }

        return filteredCommands;
    }

    @RequiredArgsConstructor
    private static class OptionComparator implements Comparator<Option<?>> {
        private final List<Option<?>> list;

        private final ToIntFunction<Option<?>> toIntFunction = value -> {
            final int index = OptionComparator.this.list.indexOf(value);
            // Required options always need to be in front of none required ones
            if (value.isRequired()) {
                return index;
            }
            return index + 1000;
        };

        @Override
        public int compare(final Option<?> o1, final Option<?> o2) {
            return Integer.compare(this.toIntFunction.applyAsInt(o1), this.toIntFunction.applyAsInt(o2));
        }
    }

    public <T> void sendArgumentCorrectionMessage(final SlashCommandParameters commandParameters,
                                                  final Option<?> option,
                                                  @Nullable final MainReplaceData mainReplaceData,
                                                  final Class<? extends SlashCommand> valueCommandClass,
                                                  final List<T> similarValues,
                                                  final Function<T, String> valueToString) {
        final SlashCommand mainCommand;
        if (mainReplaceData == null) {
            mainCommand = null;
        } else {
            mainCommand = this.getCommand(mainReplaceData.getCommandClass()).orElse(null);
        }

        final Map<Button, ButtonAction> buttons = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder(
                String.format(
                        "%s is not a valid %s.%n",
                        MarkdownUtil.monospace(commandParameters.getOptionAsString(option).orElse("Unknown")),
                        option.getName()
                )
        );

        // Only main command
        if (similarValues.isEmpty() && mainCommand != null) {
            final StringJoiner arguments = new StringJoiner(" ");
            for (final Map.Entry<String, OptionMapping> argument : mainReplaceData.getNewArgs().entrySet()) {
                arguments.add(argument.getKey() + ": " + argument.getValue().getAsString());
            }

            description.append(String.format(
                    "Use the %s command or click the %s emote to see all %ss.",
                    MarkdownUtil.bold("/" + mainCommand.getName() + " " + arguments),
                    DiscordEmotes.FOLDER.getEmote(),
                    option.getName()
            ));
        } else {
            // Contains help values
            description.append("Is it possible that you wanted to write?\n\n");

            // We can only have 5 buttons per message
            final int allowedButtons = mainCommand != null ? 4 : 5;
            for (int index = 0; Math.min(allowedButtons, similarValues.size()) > index; index++) {
                final String similarValue = valueToString.apply(similarValues.get(index));
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                description.append(String.format(
                        "%s %s%n",
                        emote,
                        similarValue
                ));

                final Map<String, OptionMapping> newOptions = new HashMap<>(commandParameters.getOptions());
                newOptions.remove(option.getName());

                // newOptions.put(option.getName(), new OptionMapping())

                // TODO: Re-add
                /*
                buttons.put(
                        Button.of(ButtonStyle.SECONDARY, emote, "").withEmoji(Emoji.fromUnicode(emote)),
                        new CommandButtonAction(
                                valueCommandClass,
                                CommandParameters.of(
                                        ArrayUtilities.modifyArrayAtPosition(
                                                commandParameters.getArgs(),
                                                similarValue,
                                                argPos
                                        ),
                                        commandParameters.isGuildCommand(),
                                        commandParameters.getCommandCause(),
                                        this,
                                        commandParameters.getChannelDb(),
                                        commandParameters.getUserDb()
                                )
                        )
                );

                 */
            }

            if (mainCommand != null) {
                description.append(String.format(
                        "%n%s %s",
                        DiscordEmotes.FOLDER.getEmote(),
                        MarkdownUtil.bold("All " + option.getName() + "s")
                ));
            }
        }


        if (mainCommand != null) {
            final SlashCommandParameters newCommandParameters = new StoredSlashCommandParameters(
                    commandParameters.getJda(),
                    commandParameters.getCommandCause(),
                    this,
                    commandParameters.getChannelDb(),
                    commandParameters.getUserDb(),
                    mainReplaceData.getNewArgs(),
                    commandParameters.getSubCommandName().orElse(null),
                    commandParameters.isGuildCommand()
            );
            final String everythingEmote = DiscordEmotes.FOLDER.getEmote();
            buttons.put(
                    Button.of(ButtonStyle.SECONDARY, everythingEmote, "")
                            .withEmoji(Emoji.fromUnicode(everythingEmote)),
                    new CommandButtonAction(
                            mainCommand.getClass(),
                            newCommandParameters
                    )
            );
        }

        // Send message
        final MessageEmbed messageEmbed = commandParameters.getEmbedBuilder()
                .setTitle("Invalid " + StringUtilities.capitalize(option.getName()))
                .setDescription(description.toString())
                .setFooter("↓ Click Me!")
                .buildSingle();

        // Handle empty actions
        // We need to handle them because jda will throw an exception otherwise
        if (buttons.isEmpty()) {
            commandParameters.sendMessage(messageEmbed);
        } else {
            commandParameters.createMessageUpdateAction(messageEmbed)
                    .setActionRows(buttons.keySet())
                    .queue(message ->
                            this.buttonReactionModule.addButtonReactionMessage(
                                    message,
                                    new ButtonReaction(
                                            buttons,
                                            commandParameters.getUserDb().getDiscordId()
                                    )
                            )
                    );
        }
    }
}
