package de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;

public interface DiscordOption {
    Mentions getMentions();
    
    String getName();

    Message.Attachment getAsAttachment();

    String getAsString();

    boolean getAsBoolean();

    long getAsLong();

    int getAsInt();

    double getAsDouble();

    IMentionable getAsMentionable();

    Member getAsMember();

    User getAsUser();

    Role getAsRole();

    ChannelType getChannelType();

    GuildChannelUnion getAsChannel();
}
