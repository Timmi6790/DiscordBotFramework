package de.timmi6790.discord_framework.module.modules.slashcommand.parameters;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDb;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.cause.CommandCause;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.action.CommandRestAction;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.action.CreateRestAction;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.DiscordOption;
import de.timmi6790.discord_framework.module.modules.user.UserDb;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class StoredSlashCommandParameters extends SlashCommandParameters {
    private final boolean guildCommand;

    public StoredSlashCommandParameters(final JDA jda, final CommandCause commandCause, final SlashCommandModule commandModule,
                                        final ChannelDb channelDb, final UserDb userDb, final Map<String, DiscordOption> options,
                                        final String subCommandName, final boolean guildCommand) {
        super(
                jda,
                commandCause,
                commandModule,
                channelDb,
                userDb,
                options,
                subCommandName
        );

        this.guildCommand = guildCommand;
    }

    private StoredSlashCommandParameters(final StoredSlashCommandParameters slashCommandParameters, final Map<String, DiscordOption> options) {
        super(slashCommandParameters, options);

        this.guildCommand = slashCommandParameters.guildCommand;
    }

    @Override
    public boolean isGuildCommand() {
        return this.guildCommand;
    }

    @Override
    public CommandRestAction createMessageUpdateAction(final Collection<? extends MessageEmbed> embeds) {
        return new CreateRestAction(this.getChannelDb().getChannel().sendMessageEmbeds(embeds));
    }

    @Override
    public CommandRestAction createMessageUpdateAction(final MessageEmbed... embeds) {
        return new CreateRestAction(this.getChannelDb().getChannel().sendMessageEmbeds(Arrays.asList(embeds)));
    }

    @Override
    public CommandRestAction createMessageUpdateAction(final String message) {
        return new CreateRestAction(this.getChannelDb().getChannel().sendMessage(message));
    }

    @Override
    public CommandRestAction createFileAction(final InputStream stream, final String name) {
        final FileUpload fileUpload = FileUpload.fromData(stream, name);
        return new CreateRestAction(this.getChannelDb().getChannel().sendFiles(fileUpload));
    }

    @Override
    public SlashCommandParameters clone(final Map<String, DiscordOption> newOptions) {
        return new StoredSlashCommandParameters(this, newOptions);
    }
}
