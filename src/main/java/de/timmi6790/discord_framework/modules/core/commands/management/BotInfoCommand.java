package de.timmi6790.discord_framework.modules.core.commands.management;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.core.ChannelDb;
import de.timmi6790.discord_framework.modules.core.GuildDb;
import de.timmi6790.discord_framework.modules.core.UserDb;

import java.util.concurrent.ThreadPoolExecutor;

public class BotInfoCommand extends AbstractCommand {
    public BotInfoCommand() {
        super("binfo", "Management", "", "");

        this.setPermission("core.management.binfo");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final int guilds = DiscordBot.getDiscord().getSelfUser().getMutualGuilds().size();

        final long userCacheSize = UserDb.getUSER_CACHE().estimatedSize();
        final double userCacheMissRate = UserDb.getUSER_CACHE().stats().missRate();

        final long channelCacheSize = ChannelDb.getCHANNEL_CACHE().estimatedSize();
        final double channelCacheMissRate = ChannelDb.getCHANNEL_CACHE().stats().missRate();

        final long guildCacheSize = GuildDb.getGUILD_CACHE().estimatedSize();
        final double guildCacheMissRate = GuildDb.getGUILD_CACHE().stats().missRate();

        final long emoteListenerSize = DiscordBot.getEmoteReactionManager().getEmoteMessageCache().estimatedSize();

        final ThreadPoolExecutor commandExecutor = DiscordBot.getCommandManager().getExecutor();
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
