package de.timmi6790.discord_framework.datatypes.builders;

import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.*;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ToString
@Getter
public class MultiEmbedBuilder {
    // Title, Description, Field, Footer.text, Author.name
    protected static final int EMBED_TOTAL_MAX = 6_000;
    protected static final int EMBED_DESCRIPTION_MAX = 2_048;
    protected static final int EMBED_FOOTER_MAX = 2_048;
    protected static final int EMBED_TITLE_MAX = 256;
    protected static final int EMBED_AUTHOR_MAX = 256;
    protected static final int EMBED_FIELD_MAX = 25;
    protected static final int EMBED_FIELD_NAME_MAX = 256;
    protected static final int EMBED_FIELD_VALUE_MAX = 1_024;
    protected static final int EMBED_URL_MAX = 2_000;

    protected static final int EMBED_DEFAULT_COLOUR = 536870911;

    private final List<Field> fields = new LinkedList<>();
    private final StringBuilder description = new StringBuilder();
    private int color = EMBED_DEFAULT_COLOUR;
    private String url;
    private String title;
    private OffsetDateTime timestamp;
    private Thumbnail thumbnail;
    private AuthorInfo author;
    private Footer footer;
    private ImageInfo image;

    public MultiEmbedBuilder() {
    }

    public MultiEmbedBuilder(@Nullable final MultiEmbedBuilder builder) {
        if (builder != null) {
            this.setDescription(builder.description.toString());
            this.fields.addAll(builder.fields);
            this.url = builder.url;
            this.title = builder.title;
            this.timestamp = builder.timestamp;
            this.color = builder.color;
            this.thumbnail = builder.thumbnail;
            this.author = builder.author;
            this.footer = builder.footer;
            this.image = builder.image;
        }

    }

    public MultiEmbedBuilder(@Nullable final MessageEmbed embed) {
        if (embed != null) {
            this.setDescription(embed.getDescription());
            this.url = embed.getUrl();
            this.title = embed.getTitle();
            this.timestamp = embed.getTimestamp();
            this.color = embed.getColorRaw();
            this.thumbnail = embed.getThumbnail();
            this.author = embed.getAuthor();
            this.footer = embed.getFooter();
            this.image = embed.getImage();
            this.fields.addAll(embed.getFields());
        }
    }

    private static int getFieldLength(final Field field) {
        int fieldSize = 0;
        if (field.getValue() != null) {
            fieldSize += field.getValue().length();
        }
        if (field.getName() != null) {
            fieldSize += field.getName().length();
        }
        return fieldSize;
    }

    private static int findMessageBreakPoint(final StringBuilder description, final int maxCutPoint) {
        final int newLineIndex = description.lastIndexOf("\n", maxCutPoint);
        if (newLineIndex != -1 && 400 > maxCutPoint - newLineIndex) {
            return newLineIndex + 1;
        }

        // Find space near break point
        final int spaceLindeIndex = description.lastIndexOf(" ", maxCutPoint);
        if (newLineIndex != -1 && 600 > maxCutPoint - spaceLindeIndex) {
            return newLineIndex;
        }

        return maxCutPoint;
    }

    private String splitDescriptionTillOne(final List<MessageEmbed> embeds) {
        String lastDescription = null;
        int breakPoint;
        for (int descriptionIndex = 0; this.description.length() > descriptionIndex; descriptionIndex = breakPoint) {
            // Don't split if we have enough for the entire remaining message
            if (descriptionIndex + EMBED_DESCRIPTION_MAX > this.description.length()) {
                breakPoint = this.description.length();
            } else {
                breakPoint = findMessageBreakPoint(this.description, descriptionIndex + EMBED_DESCRIPTION_MAX);
            }

            lastDescription = this.description.substring(descriptionIndex, breakPoint);

            // Exclude the last message
            if (this.description.length() > breakPoint) {
                embeds.add(this.createMessageEmbed(lastDescription, null, embeds.isEmpty(), false));
            }
        }

        return lastDescription;
    }

    private int getFooterLength() {
        return (this.footer == null || this.footer.getText() == null) ? 0 : this.footer.getText().length();
    }

    private int getAuthorLength() {
        return (this.author == null || this.author.getName() == null) ? 0 : this.author.getName().length();
    }

    private int getTitleLength() {
        return this.title == null ? 0 : this.title.length();
    }

    private MessageEmbed createMessageEmbed(final String description, final List<Field> fields, final boolean includeHeader, final boolean includeFooter) {
        return EntityBuilder.createMessageEmbed(
                includeHeader ? this.url : null,
                includeHeader ? this.title : null,
                description,
                EmbedType.RICH,
                includeFooter ? this.timestamp : null,
                this.color,
                includeHeader ? this.thumbnail : null,
                null,
                includeHeader ? this.author : null,
                null,
                includeFooter ? this.footer : null,
                includeHeader ? this.image : null,
                fields
        );
    }

    @Nonnull
    public MessageEmbed[] build() {
        Checks.check(!this.isEmpty(), "Cannot build an empty embed!");

        final List<MessageEmbed> embeds = new ArrayList<>();

        // Descriptions
        final String lastDescription = this.splitDescriptionTillOne(embeds);

        // Fields
        int currentEmbedSize = lastDescription != null ? lastDescription.length() : 0;
        if (embeds.isEmpty()) {
            currentEmbedSize += this.getTitleLength();
            currentEmbedSize += this.getAuthorLength();
        }

        int lastFieldIndex = 0;
        for (int fieldIndex = 0; this.fields.size() > fieldIndex; fieldIndex++) {
            final Field field = this.fields.get(fieldIndex);
            final int fieldSize = getFieldLength(field);

            // Add a new message when either the total amount, or the total amount of fields is reached.
            if (currentEmbedSize + fieldSize > EMBED_TOTAL_MAX || fieldIndex - lastFieldIndex >= EMBED_FIELD_MAX) {
                embeds.add(this.createMessageEmbed(lastFieldIndex == 0 ? lastDescription : null, this.fields.subList(lastFieldIndex, fieldIndex), embeds.isEmpty(), false));
                currentEmbedSize = 0;
                lastFieldIndex = fieldIndex;
            }

            currentEmbedSize += fieldSize;
        }

        // Last message
        // Check if we have enough space for the footer, or put the footer in a new message.
        final boolean enoughFooterSpace = EMBED_TOTAL_MAX >= this.getFooterLength() + currentEmbedSize;
        final String fieldDescription = lastFieldIndex == 0 ? lastDescription : null;
        embeds.add(this.createMessageEmbed(fieldDescription, this.fields.subList(lastFieldIndex, this.fields.size()), embeds.isEmpty(), enoughFooterSpace));

        if (!enoughFooterSpace) {
            embeds.add(this.createMessageEmbed(null, null, false, true));
        }

        return embeds.toArray(new MessageEmbed[0]);
    }

    @Nonnull
    public MessageEmbed buildSingle() {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot build an empty embed!");
        } else if (this.description.length() > EMBED_DESCRIPTION_MAX) {
            throw new IllegalStateException(String.format("Description is longer than %d! Please limit your input!", EMBED_DESCRIPTION_MAX));
        } else if (this.length() > EMBED_TOTAL_MAX) {
            throw new IllegalStateException("Cannot build an embed with more than 6000 characters!");
        } else {
            final String description = this.description.length() < 1 ? null : this.description.toString();
            return EntityBuilder.createMessageEmbed(this.url, this.title, description, EmbedType.RICH, this.timestamp, this.color, this.thumbnail,
                    null, this.author, null, this.footer, this.image, this.fields);
        }
    }

    @Nonnull
    public MultiEmbedBuilder clear() {
        this.description.setLength(0);
        this.fields.clear();
        this.url = null;
        this.title = null;
        this.timestamp = null;
        this.color = EMBED_DEFAULT_COLOUR;
        this.thumbnail = null;
        this.author = null;
        this.footer = null;
        this.image = null;
        return this;
    }

    public boolean isEmpty() {
        return this.title == null &&
                this.timestamp == null &&
                this.thumbnail == null &&
                this.author == null &&
                this.footer == null &&
                this.image == null &&
                this.color == EMBED_DEFAULT_COLOUR &&
                this.description.length() == 0 &&
                this.fields.isEmpty();
    }

    public int length() {
        int length = this.description.length();
        for (final Field field : this.fields) {
            length += getFieldLength(field);
        }

        length += this.getTitleLength();
        length += this.getAuthorLength();
        length += this.getFooterLength();
        return length;
    }

    @Nonnull
    public MultiEmbedBuilder setTitle(@Nullable final String title) {
        return this.setTitle(title, null);
    }

    @Nonnull
    public MultiEmbedBuilder setTitle(@Nullable final String title, @Nullable String url) {
        if (title == null) {
            this.title = null;
            this.url = null;
        } else {
            Checks.notEmpty(title, "Title");
            Checks.check(title.length() <= EMBED_TITLE_MAX, "Title cannot be longer than %d characters.", EMBED_TITLE_MAX);
            if (Helpers.isBlank(url)) {
                url = null;
            }

            this.urlCheck(url);
            this.title = title;
            this.url = url;
        }

        return this;
    }

    @Nonnull
    public StringBuilder getDescriptionBuilder() {
        return this.description;
    }

    @Nonnull
    public MultiEmbedBuilder setDescription(final String format, final Object... objects) {
        return this.setDescription(String.format(format, objects));
    }

    @Nonnull
    public MultiEmbedBuilder setDescription(@Nullable final CharSequence description) {
        this.description.setLength(0);
        if (description != null && description.length() >= 1) {
            this.appendDescription(description);
        }

        return this;
    }

    @Nonnull
    public MultiEmbedBuilder appendDescription(final String format, final Object... object) {
        return this.appendDescription(String.format(format, object));
    }

    @Nonnull
    public MultiEmbedBuilder appendDescription(@Nonnull final CharSequence description) {
        Checks.notNull(description, "description");
        this.description.append(description);
        return this;
    }

    @Nonnull
    public MultiEmbedBuilder setTimestamp(@Nullable final TemporalAccessor temporal) {
        if (temporal == null) {
            this.timestamp = null;
        } else if (temporal instanceof OffsetDateTime) {
            this.timestamp = (OffsetDateTime) temporal;
        } else {
            ZoneOffset offset;
            try {
                offset = ZoneOffset.from(temporal);
            } catch (final DateTimeException var7) {
                offset = ZoneOffset.UTC;
            }

            try {
                final LocalDateTime ldt = LocalDateTime.from(temporal);
                this.timestamp = OffsetDateTime.of(ldt, offset);
            } catch (final DateTimeException var6) {
                try {
                    final Instant instant = Instant.from(temporal);
                    this.timestamp = OffsetDateTime.ofInstant(instant, offset);
                } catch (final DateTimeException var5) {
                    throw new DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), var5);
                }
            }
        }

        return this;
    }

    @Nonnull
    public MultiEmbedBuilder setColor(@Nullable final Color color) {
        this.color = color == null ? EMBED_DEFAULT_COLOUR : color.getRGB();
        return this;
    }

    @Nonnull
    public MultiEmbedBuilder setColor(final int color) {
        this.color = color;
        return this;
    }

    @Nonnull
    public MultiEmbedBuilder setThumbnail(@Nullable final String url) {
        if (url == null) {
            this.thumbnail = null;
        } else {
            this.urlCheck(url);
            this.thumbnail = new Thumbnail(url, null, 0, 0);
        }

        return this;
    }

    @Nonnull
    public MultiEmbedBuilder setImage(@Nullable final String url) {
        if (url == null) {
            this.image = null;
        } else {
            this.urlCheck(url);
            this.image = new ImageInfo(url, null, 0, 0);
        }

        return this;
    }

    @Nonnull
    public MultiEmbedBuilder setAuthor(@Nullable final String name) {
        return this.setAuthor(name, null, null);
    }

    @Nonnull
    public MultiEmbedBuilder setAuthor(@Nullable final String name, @Nullable final String url) {
        return this.setAuthor(name, url, null);
    }

    @Nonnull
    public MultiEmbedBuilder setAuthor(@Nullable final String name, @Nullable final String url, @Nullable final String iconUrl) {
        if (name == null) {
            this.author = null;
        } else {
            Checks.check(name.length() <= EMBED_AUTHOR_MAX, "Author name cannot be longer than %d characters.", EMBED_AUTHOR_MAX);
            this.urlCheck(url);
            this.urlCheck(iconUrl);
            this.author = new AuthorInfo(name, url, iconUrl, null);
        }

        return this;
    }

    @Nonnull
    public MultiEmbedBuilder setFooter(@Nullable final String text) {
        return this.setFooter(text, null);
    }

    @Nonnull
    public MultiEmbedBuilder setFooter(@Nullable final String text, @Nullable final String iconUrl) {
        if (text == null) {
            this.footer = null;
        } else {
            Checks.check(text.length() <= EMBED_FOOTER_MAX, "Text cannot be longer than %d characters.", EMBED_FOOTER_MAX);
            this.urlCheck(iconUrl);
            this.footer = new Footer(text, iconUrl, null);
        }

        return this;
    }

    public MultiEmbedBuilder addField(@Nullable final String name, @Nullable final String value, final boolean inline, final boolean ifCondition) {
        if (ifCondition) {
            this.addField(name, value, inline);
        }

        return this;
    }

    @Nonnull
    public MultiEmbedBuilder addField(@Nullable final Field field) {
        return field == null ? this : this.addField(field.getName(), field.getValue(), field.isInline());
    }

    public MultiEmbedBuilder addField(final String name, final String value) {
        return this.addField(name, value, false);
    }

    @Nonnull
    public MultiEmbedBuilder addField(@Nullable final String name, @Nullable final String value, final boolean inline) {
        if (name != null || value != null) {
            Checks.check(name == null || name.length() <= EMBED_FIELD_NAME_MAX, "Name cannot be longer than %d characters.", EMBED_FIELD_NAME_MAX);
            Checks.check(value == null || value.length() <= EMBED_FIELD_VALUE_MAX, "Value cannot be longer than %d characters.", EMBED_FIELD_VALUE_MAX);
            this.fields.add(new Field(name, value, inline));
        }

        return this;
    }

    @Nonnull
    public MultiEmbedBuilder addBlankField(final boolean inline) {
        this.fields.add(new Field(EmbedBuilder.ZERO_WIDTH_SPACE, EmbedBuilder.ZERO_WIDTH_SPACE, inline));
        return this;
    }

    @Nonnull
    public MultiEmbedBuilder clearFields() {
        this.fields.clear();
        return this;
    }

    private void urlCheck(@Nullable final String url) {
        if (url != null) {
            Checks.check(url.length() <= EMBED_URL_MAX, "URL cannot be longer than %d characters.", EMBED_URL_MAX);
            Checks.check(EmbedBuilder.URL_PATTERN.matcher(url).matches(), "URL must be a valid http(s) or attachment url.");
        }
    }
}
