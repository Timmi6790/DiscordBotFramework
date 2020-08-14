package de.timmi6790.discord_framework.modules.core.commands.management;

import de.timmi6790.discord_framework.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.core.CoreModule;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.modules.user.UserDbModule;

import java.util.concurrent.ThreadPoolExecutor;

public class BotInfoCommand extends AbstractCommand<CoreModule> {
    public BotInfoCommand() {
        super("binfo", "Management", "", "");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final int guilds = this.getModule().getDiscord().getSelfUser().getMutualGuilds().size();

        final long userCacheSize = this.getModule().getModuleOrThrow(UserDbModule.class).getCache().estimatedSize();
        final double userCacheMissRate = this.getModule().getModuleOrThrow(UserDbModule.class).getCache().stats().missRate();

        final long channelCacheSize = this.getModule().getModuleOrThrow(ChannelDbModule.class).getCache().estimatedSize();
        final double channelCacheMissRate = this.getModule().getModuleOrThrow(ChannelDbModule.class).getCache().stats().missRate();

        final long guildCacheSize = this.getModule().getModuleOrThrow(GuildDbModule.class).getCache().estimatedSize();
        final double guildCacheMissRate = this.getModule().getModuleOrThrow(GuildDbModule.class).getCache().stats().missRate();

        final long emoteListenerSize = this.getModule().getModuleOrThrow(EmoteReactionModule.class).getEmoteMessageCache().estimatedSize();

        final ThreadPoolExecutor commandExecutor = this.getModule().getModuleOrThrow(CommandModule.class).getExecutor();
        final long activeCommands = commandExecutor.getActiveCount();
        final long queuedCommands = commandExecutor.getQueue().size();
        final long totalCommands = commandExecutor.getTaskCount();

        this.sendTimedMessage(commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Bot Info")
                        .addField("Guilds", String.valueOf(guilds), true)
                        .addField("User Cache", userCacheSize + ";" + userCacheMissRate, true)
                        .addField("Channel Cache", channelCacheSize + ";" + channelCacheMissRate, true)
                        .addField("Guild Cache", guildCacheSize + ";" + guildCacheMissRate, true)
                        .addField("Active Emotes", String.valueOf(emoteListenerSize), true)
                        .addField("Commands", activeCommands + ";" + queuedCommands + ";" + totalCommands, true),
                90
        );

        return CommandResult.SUCCESS;
    }
}
