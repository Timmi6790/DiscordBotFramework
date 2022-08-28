package de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@RequiredArgsConstructor
public class RawDiscordOption implements DiscordOption {
    private final OptionMapping raw;

    @Override
    public Mentions getMentions() {
        return this.raw.getMentions();
    }

    @Override
    public String getName() {
        return this.raw.getName();
    }

    @Override
    public Message.Attachment getAsAttachment() {
        return this.raw.getAsAttachment();
    }

    @Override
    public String getAsString() {
        return this.raw.getAsString();
    }

    @Override
    public boolean getAsBoolean() {
        return this.raw.getAsBoolean();
    }

    @Override
    public long getAsLong() {
        return this.raw.getAsLong();
    }

    @Override
    public int getAsInt() {
        return this.raw.getAsInt();
    }

    @Override
    public double getAsDouble() {
        return this.raw.getAsDouble();
    }

    @Override
    public IMentionable getAsMentionable() {
        return this.raw.getAsMentionable();
    }

    @Override
    public Member getAsMember() {
        return this.raw.getAsMember();
    }

    @Override
    public User getAsUser() {
        return this.raw.getAsUser();
    }

    @Override
    public Role getAsRole() {
        return this.raw.getAsRole();
    }

    @Override
    public ChannelType getChannelType() {
        return this.raw.getChannelType();
    }

    @Override
    public GuildChannelUnion getAsChannel() {
        return this.raw.getAsChannel();
    }
}
