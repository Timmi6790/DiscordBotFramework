package de.timmi6790.discord_framework.module.modules.slashcommand.parameters;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.cause.CommandCause;
import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.action.CommandRestAction;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.DiscordOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.RawDiscordOption;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.AttachmentOption;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Data
public abstract class SlashCommandParameters implements Cloneable {
    public static Map<String, DiscordOption> formatEventOptions(final List<OptionMapping> options) {
        final Map<String, DiscordOption> formatOptions = new HashMap<>(options.size());
        for (final OptionMapping option : options) {
            formatOptions.put(option.getName(), new RawDiscordOption(option));
        }
        return formatOptions;
    }

    public static Map<String, DiscordOption> formatOptions(final List<DiscordOption> options) {
        final Map<String, DiscordOption> formatOptions = new HashMap<>(options.size());
        for (final DiscordOption option : options) {
            formatOptions.put(option.getName(), option);
        }
        return formatOptions;
    }

    private final JDA jda;
    private final CommandCause commandCause;
    private final SlashCommandModule commandModule;
    private final ChannelDb channelDb;
    private final UserDb userDb;
    private final Map<String, DiscordOption> options;
    private final String subCommandName;

    public GuildDb getGuildDb() {
        return this.channelDb.getGuildDb();
    }

    public Member getGuildMember() {
        return this.getGuildDb().getMember(this.getUser());
    }

    public abstract boolean isGuildCommand();

    public abstract CommandRestAction createMessageUpdateAction(final Collection<? extends MessageEmbed> embeds);

    public abstract CommandRestAction createMessageUpdateAction(final MessageEmbed... embeds);

    public abstract CommandRestAction createMessageUpdateAction(final String message);

    public abstract CommandRestAction createFileAction(final InputStream stream, final String name, final AttachmentOption... option);

    public CommandRestAction createMessageUpdateAction(final MultiEmbedBuilder builder) {
        return this.createMessageUpdateAction(builder.build());
    }

    public void sendMessage(final MultiEmbedBuilder builder) {
        this.createMessageUpdateAction(builder).queue();
    }

    public void sendMessage(final Collection<? extends MessageEmbed> embeds) {
        this.createMessageUpdateAction(embeds).queue();
    }

    public void sendMessage(final MessageEmbed... embeds) {
        this.createMessageUpdateAction(embeds).queue();
    }

    public void sendMessage(final String message) {
        this.createMessageUpdateAction(message).queue();
    }

    public void sendFile(final InputStream stream, final String name, final AttachmentOption... options) {
        this.createFileAction(stream, name, options).queue();
    }

    public Optional<DiscordOption> getOptionalMapping(final String optionName) {
        return Optional.ofNullable(this.options.get(optionName));
    }

    public boolean hasOption(final Option<?> option) {
        return this.getOptionalMapping(option.getName()).isPresent();
    }

    public <T> Optional<T> getOption(final Option<T> option) {
        return this.getOptionalMapping(option.getName())
                .flatMap(option::convertValue);
    }

    public <T> T getOptionOrThrow(final Option<T> option) {
        return this.getOption(option)
                .orElseThrow(CommandReturnException::new);
    }

    public Optional<String> getOptionAsString(final Option<?> option) {
        return this.getOptionalMapping(option.getName())
                .map(DiscordOption::getAsString);
    }

    public JDA getJda() {
        return this.jda;
    }

    public User getUser() {
        return this.userDb.getUser();
    }

    public Optional<String> getSubCommandName() {
        return Optional.ofNullable(this.subCommandName);
    }

    @SneakyThrows
    public PrivateChannel getUserTextChannel() {
        final CompletableFuture<PrivateChannel> futureValue = new CompletableFuture<>();
        this.getUser().openPrivateChannel().queue(futureValue::complete);
        return futureValue.get(1, TimeUnit.MINUTES);
    }

    // Messages
    public MultiEmbedBuilder getEmbedBuilder() {
        return DiscordMessagesUtilities.getEmbedBuilder(
                this.getUser(),
                this.isGuildCommand() ? this.getGuildMember() : null
        );
    }

    public void sendPrivateMessage(final MultiEmbedBuilder builder) {
        DiscordMessagesUtilities.sendPrivateMessage(
                this.userDb.getUser(),
                builder
        );
    }

    @SneakyThrows
    public SlashCommandParameters clone(final Map<String, DiscordOption> newOptions) {
        final SlashCommandParameters commandParameters = (SlashCommandParameters) super.clone();

        commandParameters.options.clear();
        commandParameters.options.putAll(newOptions);

        return commandParameters;
    }
}
