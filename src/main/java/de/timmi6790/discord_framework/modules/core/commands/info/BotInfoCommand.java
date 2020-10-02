package de.timmi6790.discord_framework.modules.core.commands.info;

import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.EqualsAndHashCode;

import java.util.concurrent.ThreadPoolExecutor;

@EqualsAndHashCode(callSuper = true)
public class BotInfoCommand extends AbstractCommand {
    private final UserDbModule userDbModule;
    private final ChannelDbModule channelDbModule;
    private final GuildDbModule guildDbModule;
    private final EmoteReactionModule emoteReactionModule;

    public BotInfoCommand() {
        super("binfo", "Management", "", "");


        this.userDbModule = getModuleManager().getModuleOrThrow(UserDbModule.class);
        this.channelDbModule = getModuleManager().getModuleOrThrow(ChannelDbModule.class);
        this.guildDbModule = getModuleManager().getModuleOrThrow(GuildDbModule.class);
        this.emoteReactionModule = getModuleManager().getModuleOrThrow(EmoteReactionModule.class);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final int guilds = this.getDiscord().getSelfUser().getMutualGuilds().size();

        final long userCacheSize = this.userDbModule.getCache().estimatedSize();
        final long channelCacheSize = this.channelDbModule.getCache().estimatedSize();
        final long guildCacheSize = this.guildDbModule.getCache().estimatedSize();
        final long emoteListenerSize = this.emoteReactionModule.getEmoteMessageCache().estimatedSize();

        final ThreadPoolExecutor commandExecutor = this.getCommandModule().getExecutor();
        final long activeCommands = commandExecutor.getActiveCount();
        final long queuedCommands = commandExecutor.getQueue().size();
        final long totalCommands = commandExecutor.getTaskCount();

        sendTimedMessage(commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Bot Info")
                        .addField("Guilds", String.valueOf(guilds), true)
                        .addField("User Cache", String.valueOf(userCacheSize), true)
                        .addField("Channel Cache", String.valueOf(channelCacheSize), true)
                        .addField("Guild Cache", String.valueOf(guildCacheSize), true)
                        .addField("Active Emotes", String.valueOf(emoteListenerSize), true)
                        .addField("Commands", activeCommands + ";" + queuedCommands + ";" + totalCommands, true),
                90
        );

        return CommandResult.SUCCESS;
    }
}
