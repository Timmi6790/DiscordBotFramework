package de.timmi6790.discord_framework.module.modules.command;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.command.commands.HelpCommand;
import de.timmi6790.discord_framework.module.modules.command.listeners.MessageListener;
import de.timmi6790.discord_framework.module.modules.command.listeners.MetricListener;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.command.utilities.ArrayUtilities;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.event.EventModule;
import de.timmi6790.discord_framework.module.modules.metric.MetricModule;
import de.timmi6790.discord_framework.module.modules.permisssion.PermissionsModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReaction;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReactionModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.CommandButtonAction;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import de.timmi6790.discord_framework.utilities.commons.StringUtilities;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
@Log4j2
public class CommandModule extends AbstractModule {
    private final Map<String, Command> commands = new CaseInsensitiveMap<>();
    private final Map<String, String> commandAliases = new CaseInsensitiveMap<>();

    private Config config;

    @Getter(AccessLevel.PUBLIC)
    private PermissionsModule permissionsModule;
    @Getter(AccessLevel.PUBLIC)
    private EventModule eventModule;
    private MetricModule metricModule;
    @Getter(AccessLevel.PUBLIC)
    private ButtonReactionModule buttonReactionModule;

    public CommandModule() {
        super("Command");

        this.addDiscordGatewayIntents(
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGES
        );

        this.addDependenciesAndLoadAfter(
                EventModule.class,
                ConfigModule.class,
                PermissionsModule.class,
                ButtonReactionModule.class
        );

        this.addLoadAfterDependencies(
                MetricModule.class
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
        this.buttonReactionModule = this.getModuleOrThrow(ButtonReactionModule.class);

        this.config = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());

        this.registerCommand(
                this,
                new HelpCommand(
                        this
                )
        );

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
            this.getDiscord().setActivity(Activity.playing(this.getMainCommand() + "help"));
        }

        // We currently need to register this during on enable, because otherwise we  have topical sort cycle.
        // This should be resolved with the dpi system
        this.getModuleOrThrow(EventModule.class).addEventListeners(
                new MessageListener(
                        this,
                        this.getModuleOrThrow(UserDbModule.class),
                        this.getModuleOrThrow(ChannelDbModule.class),
                        this.buttonReactionModule,
                        this.getCommand(HelpCommand.class).orElseThrow(RuntimeException::new)
                )
        );

        return true;
    }

    protected String getCommandPermissionNode(final AbstractModule module, final Command command) {
        return String.format(
                        "%s.command.%s",
                        module.getModuleName(),
                        command.getName()
                )
                .replace(' ', '_')
                .toLowerCase();
    }

    public Optional<MetricModule> getMetricModule() {
        return Optional.ofNullable(this.metricModule);
    }

    public String getMainCommand() {
        return this.config.getMainCommand();
    }

    public long getBotId() {
        return this.getDiscordBot().getBaseShard().getSelfUser().getIdLong();
    }

    public Optional<Command> getCommand(final Class<? extends Command> commandClass) {
        for (final Command command : this.commands.values()) {
            if (command.getClass() == commandClass) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }

    public Optional<Command> getCommand(String commandName) {
        commandName = this.commandAliases.getOrDefault(commandName, commandName);
        return Optional.ofNullable(this.commands.get(commandName));
    }

    public Set<Command> getCommands() {
        return new HashSet<>(this.commands.values());
    }

    public Set<Command> getCommands(final Predicate<Command> commandPredicate) {
        final Set<Command> filteredCommands = new HashSet<>();

        for (final Command command : this.commands.values()) {
            if (commandPredicate.test(command)) {
                filteredCommands.add(command);
            }
        }

        return filteredCommands;
    }

    public void registerCommands(final AbstractModule module, final Command... commands) {
        for (final Command command : commands) {
            this.registerCommand(module, command);
        }
    }

    public boolean registerCommand(final AbstractModule module, final Command command) {
        if (this.commands.containsKey(command.getName())) {
            log.warn(
                    "The module {} tried to register the {} command that already exists.",
                    module.getModuleName(),
                    command.getName()
            );
            return false;
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
        this.commands.put(command.getName(), command);
        for (final String aliasName : command.getPropertyValueOrDefault(AliasNamesProperty.class, () -> new String[0])) {
            final String existingAliasName = this.commandAliases.get(aliasName);
            if (existingAliasName == null) {
                this.commandAliases.put(aliasName, command.getName());
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

        return true;
    }

    // TODO: Rewrite this at one point
    public <T> void sendArgumentCorrectionMessage(final CommandParameters commandParameters,
                                                  final String userArg,
                                                  final int argPos,
                                                  final String argName,
                                                  @Nullable final Class<? extends Command> mainCommandClass,
                                                  final String[] mainNewArgs,
                                                  final Class<? extends Command> valueCommandClass,
                                                  final List<T> similarValues,
                                                  final Function<T, String> valueToString) {
        final Command mainCommand;
        if (mainCommandClass == null) {
            mainCommand = null;
        } else {
            mainCommand = this.getCommand(mainCommandClass).orElse(null);
        }

        final Map<Button, ButtonAction> buttons = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder(
                String.format(
                        "%s is not a valid %s.%n",
                        MarkdownUtil.monospace(userArg),
                        argName
                )
        );

        // Only main command
        if (similarValues.isEmpty() && mainCommand != null) {
            description.append(String.format(
                    "Use the %s command or click the %s emote to see all %ss.",
                    MarkdownUtil.bold(
                            String.join(" ",
                                    this.getMainCommand(),
                                    mainCommand.getName(),
                                    String.join(" ", mainNewArgs)
                            )
                    ),
                    DiscordEmotes.FOLDER.getEmote(),
                    argName
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
            }

            if (mainCommand != null) {
                description.append(String.format(
                        "%n%s %s",
                        DiscordEmotes.FOLDER.getEmote(),
                        MarkdownUtil.bold("All " + argName + "s")
                ));
            }
        }


        if (mainCommand != null) {
            final CommandParameters newCommandParameters = CommandParameters.of(
                    mainNewArgs,
                    commandParameters.isGuildCommand(),
                    commandParameters.getCommandCause(),
                    this,
                    commandParameters.getChannelDb(),
                    commandParameters.getUserDb()
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
                .setTitle("Invalid " + StringUtilities.capitalize(argName))
                .setDescription(description.toString())
                .setFooter("↓ Click Me!")
                .buildSingle();

        // Handle empty actions
        // We need to handle them because jda will throw an exception otherwise
        if (buttons.isEmpty()) {
            commandParameters.getLowestMessageChannel()
                    .sendMessageEmbeds(messageEmbed)
                    .queue();
        } else {
            commandParameters.getLowestMessageChannel()
                    .sendMessageEmbeds(messageEmbed)
                    .setActionRows(ActionRow.of(buttons.keySet()))
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
