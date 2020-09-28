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

        final long channelCacheSize = this.getModule().getModuleOrThrow(ChannelDbModule.class).getCache().estimatedSize();

        final long guildCacheSize = this.getModule().getModuleOrThrow(GuildDbModule.class).getCache().estimatedSize();

        final long emoteListenerSize = this.getModule().getModuleOrThrow(EmoteReactionModule.class).getEmoteMessageCache().estimatedSize();

        final ThreadPoolExecutor commandExecutor = this.getModule().getModuleOrThrow(CommandModule.class).getExecutor();
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
