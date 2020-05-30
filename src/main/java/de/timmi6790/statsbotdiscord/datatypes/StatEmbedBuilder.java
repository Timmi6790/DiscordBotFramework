package de.timmi6790.statsbotdiscord.datatypes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.temporal.TemporalAccessor;

public class StatEmbedBuilder extends EmbedBuilder {
    public StatEmbedBuilder() {
        super();
    }

    public StatEmbedBuilder(@Nullable final EmbedBuilder builder) {
        super(builder);
    }

    public StatEmbedBuilder(@Nullable final MessageEmbed embed) {
        super(embed);
    }

    public StatEmbedBuilder addField(@Nullable final String name, @Nullable final String value, final boolean inline, final boolean ifCondition) {
        if (ifCondition) {
            this.addField(name, value, inline);
        }

        return this;
    }

    public final StatEmbedBuilder setStatDescription(@Nullable final CharSequence description) {
        super.setDescription(description);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder clear() {
        super.clear();
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder setTitle(@Nullable final String title) {
        return this.setTitle(title, null);
    }

    @Override
    public @NotNull StatEmbedBuilder setTitle(@Nullable final String title, @Nullable final String url) {
        super.setTitle(title, url);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder appendDescription(@Nonnull final CharSequence description) {
        super.appendDescription(description);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder setTimestamp(@Nullable final TemporalAccessor temporal) {
        super.setTimestamp(temporal);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder setColor(@Nullable final Color color) {
        super.setColor(color);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder setColor(final int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder setThumbnail(@Nullable final String url) {
        super.setThumbnail(url);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder setImage(@Nullable final String url) {
        super.setImage(url);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder setAuthor(@Nullable final String name) {
        return this.setAuthor(name, null, null);
    }

    @Override
    public @NotNull StatEmbedBuilder setAuthor(@Nullable final String name, @Nullable final String url) {
        return this.setAuthor(name, url, null);
    }

    @Override
    public @NotNull StatEmbedBuilder setAuthor(@Nullable final String name, @Nullable final String url, @Nullable final String iconUrl) {
        super.setAuthor(name, url, iconUrl);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder setFooter(@Nullable final String text) {
        return this.setFooter(text, null);
    }

    @Override
    public @NotNull StatEmbedBuilder setFooter(@Nullable final String text, @Nullable final String iconUrl) {
        super.setFooter(text, iconUrl);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder addField(@Nullable final MessageEmbed.Field field) {
        super.addField(field);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder addField(@Nullable final String name, @Nullable final String value, final boolean inline) {
        super.addField(name, value, inline);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder addBlankField(final boolean inline) {
        super.addBlankField(inline);
        return this;
    }

    @Override
    public @NotNull StatEmbedBuilder clearFields() {
        super.clearFields();
        return this;
    }
}
