package de.timmi6790.discord_framework.module.modules.slashcommand;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.CommandCause;
import de.timmi6790.discord_framework.module.modules.guild.GuildDb;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.Data;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Collection;
import java.util.Optional;

@Data
public class SlashCommandParameters {
    private final SlashCommandInteractionEvent event;
    private final CommandCause commandCause;
    private final SlashCommandModule commandModule;
    private final ChannelDb channelDb;
    private final UserDb userDb;

    public GuildDb getGuildDb() {
        return this.channelDb.getGuildDb();
    }

    public boolean isFromGuild() {
        return this.event.isFromGuild();
    }

    public Optional<OptionMapping> getOptionalMapping(final String optionName) {
        for (final OptionMapping mapping : this.event.getOptions()) {
            if (mapping.getName().equals(optionName)) {
                return Optional.of(mapping);
            }
        }
        return Optional.empty();
    }

    public boolean hasOption(final Option<?> option) {
        return this.getOptionalMapping(option.getName()).isPresent();
    }

    public <T> Optional<T> getOption(final Option<T> option) {
        return this.getOptionalMapping(option.getName())
                .flatMap(option::convertValue);
    }

    public <T> T getOptionOrThrow(final Option<T> option) {
        final Optional<OptionMapping> mappingOpt = this.getOptionalMapping(option.getName());
        if (mappingOpt.isPresent()) {
            return mappingOpt
                    .map(option::convertValueThrow)
                    .orElseThrow(CommandReturnException::new);
        }

        // Send message
        throw new CommandReturnException();
    }

    public InteractionHook getHook() {
        return this.event.getHook();
    }

    public void sendMessage(final MultiEmbedBuilder builder) {
        this.sendMessage(builder.build());
    }

    public void sendMessage(final Collection<? extends MessageEmbed> embeds) {
        this.getHook().editOriginalEmbeds(embeds).queue();
    }

    public JDA getJda() {
        return this.event.getJDA();
    }

    // Messages
    public MultiEmbedBuilder getEmbedBuilder() {
        return DiscordMessagesUtilities.getEmbedBuilder(
                this.userDb.getUser(),
                this.isFromGuild() ? this.event.getMember() : null
        );
    }

    public void sendPrivateMessage(final MultiEmbedBuilder builder) {
        DiscordMessagesUtilities.sendPrivateMessage(
                this.userDb.getUser(),
                builder
        );
    }
}
