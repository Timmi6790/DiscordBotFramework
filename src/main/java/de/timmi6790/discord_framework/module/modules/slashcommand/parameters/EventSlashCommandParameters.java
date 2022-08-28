package de.timmi6790.discord_framework.module.modules.slashcommand.parameters;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.cause.CommandCause;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.action.CommandRestAction;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.action.FileRestAction;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.action.UpdateRestAction;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.AttachmentOption;

import java.io.InputStream;
import java.util.Collection;

public class EventSlashCommandParameters extends SlashCommandParameters {
    private final SlashCommandInteractionEvent event;

    public EventSlashCommandParameters(final SlashCommandInteractionEvent event, final CommandCause commandCause, final SlashCommandModule commandModule, final ChannelDb channelDb, final UserDb userDb) {
        super(
                event.getJDA(),
                commandCause,
                commandModule,
                channelDb,
                userDb,
                formatOptions(event.getOptions()),
                event.getSubcommandName()
        );

        this.event = event;
    }

    @Override
    public boolean isGuildCommand() {
        return this.event.isGuildCommand();
    }

    @Override
    public CommandRestAction createMessageUpdateAction(final Collection<? extends MessageEmbed> embeds) {
        return new UpdateRestAction(this.getHook().editOriginalEmbeds(embeds));
    }

    @Override
    public CommandRestAction createMessageUpdateAction(final MessageEmbed... embeds) {
        return new UpdateRestAction(this.getHook().editOriginalEmbeds(embeds));
    }

    @Override
    public CommandRestAction createMessageUpdateAction(final String message) {
        return new UpdateRestAction(this.getHook().editOriginal(message));
    }

    @Override
    public CommandRestAction createFileAction(final InputStream stream, final String name, final AttachmentOption... options) {
        return new FileRestAction(this.getHook().sendFile(stream, name, options));
    }

    public InteractionHook getHook() {
        return this.event.getHook();
    }
}
