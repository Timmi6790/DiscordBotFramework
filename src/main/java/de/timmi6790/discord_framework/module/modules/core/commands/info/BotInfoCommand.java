package de.timmi6790.discord_framework.module.modules.core.commands.info;

import de.timmi6790.discord_framework.module.modules.channel.ChannelDbModule;
import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.guild.GuildDbModule;
import de.timmi6790.discord_framework.module.modules.reactions.emote.EmoteReactionModule;
import de.timmi6790.discord_framework.module.modules.user.UserDbModule;
import lombok.EqualsAndHashCode;

/**
 * Bot info command
 */
@EqualsAndHashCode(callSuper = true)
public class BotInfoCommand extends AbstractCommand {
    /**
     * The User db module.
     */
    private final UserDbModule userDbModule;
    /**
     * The Channel db module.
     */
    private final ChannelDbModule channelDbModule;
    /**
     * The Guild db module.
     */
    private final GuildDbModule guildDbModule;
    /**
     * The Emote reaction module.
     */
    private final EmoteReactionModule emoteReactionModule;

    /**
     * Instantiates a new Bot info command.
     */
    public BotInfoCommand() {
        super("binfo", "Management", "Show the bot info", "");

        this.userDbModule = this.getModuleManager().getModuleOrThrow(UserDbModule.class);
        this.channelDbModule = this.getModuleManager().getModuleOrThrow(ChannelDbModule.class);
        this.guildDbModule = this.getModuleManager().getModuleOrThrow(GuildDbModule.class);
        this.emoteReactionModule = this.getModuleManager().getModuleOrThrow(EmoteReactionModule.class);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // discord stats
        final long guilds = this.getDiscord().getGuildCache().size();
        final int shards = this.getDiscord().getShardsTotal();

        // Module stats
        final long userCacheSize = this.userDbModule.getCache().estimatedSize();
        final long channelCacheSize = this.channelDbModule.getCache().estimatedSize();
        final long guildCacheSize = this.guildDbModule.getCache().estimatedSize();
        final long emoteListenerSize = this.emoteReactionModule.getMessageCache().estimatedSize();

        this.sendTimedMessage(commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Bot Info")
                        .addField("Guilds", String.valueOf(guilds), true)
                        .addField("Shards", String.valueOf(shards), true)
                        .addField("User Cache", String.valueOf(userCacheSize), true)
                        .addField("Channel Cache", String.valueOf(channelCacheSize), true)
                        .addField("Guild Cache", String.valueOf(guildCacheSize), true)
                        .addField("Active Emotes", String.valueOf(emoteListenerSize), true)
        );

        return CommandResult.SUCCESS;
    }
}
