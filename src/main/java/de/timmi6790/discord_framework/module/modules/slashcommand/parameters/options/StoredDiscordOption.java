package de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;

@AllArgsConstructor
public class StoredDiscordOption implements DiscordOption {
    private final String name;
    private final String value;

    @Override
    public Mentions getMentions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Message.Attachment getAsAttachment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAsString() {
        return this.value;
    }

    @Override
    public boolean getAsBoolean() {
        return Boolean.parseBoolean(this.value);
    }

    @Override
    public long getAsLong() {
        return Long.getLong(this.value);
    }

    @Override
    public int getAsInt() {
        return Integer.parseInt(this.value);
    }

    @Override
    public double getAsDouble() {
        return Double.parseDouble(this.value);
    }

    @Override
    public IMentionable getAsMentionable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Member getAsMember() {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getAsUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Role getAsRole() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelType getChannelType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GuildChannelUnion getAsChannel() {
        throw new UnsupportedOperationException();
    }
}
