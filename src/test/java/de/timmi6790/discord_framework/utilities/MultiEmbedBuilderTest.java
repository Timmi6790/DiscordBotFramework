package de.timmi6790.discord_framework.utilities;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiEmbedBuilderTest {
    private static MultiEmbedBuilder getFilledEmbedBuilder() {
        return new MultiEmbedBuilder()
                .setDescription("Description")
                .setTitle("Title")
                .setFooter("Footer")
                .setAuthor("Author")
                .addField("Field", "Value")
                .addField("D", "A");
    }

    private static String getRandomString(final int size) {
        final StringBuilder stringBuilder = new StringBuilder(size);
        final String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int count = 0; size > count; count++) {
            stringBuilder.append(abc.charAt(ThreadLocalRandom.current().nextInt(abc.length())));
        }

        return stringBuilder.toString();
    }

    @SneakyThrows
    private static String getContentFromFile(@NonNull final String path) {
        final ClassLoader classLoader = MultiEmbedBuilderTest.class.getClassLoader();

        final URI uri = classLoader.getResource(path).toURI();
        final byte[] encoded = Files.readAllBytes(Paths.get(uri));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    @Test
    void empty() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        assertThrows(IllegalArgumentException.class, embedBuilder::build);
    }

    @Test
    void emptyDescription() {
        final String title = "Test";
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder()
                .setTitle(title);

        final MessageEmbed[] messageEmbeds = embedBuilder.build().toArray(new MessageEmbed[0]);
        assertThat(messageEmbeds)
                .hasSize(1)
                .matches(new TotalSizePredicate(), "Total size")
                .matches(new OrderPredicate(), "Order");

        assertThat(messageEmbeds[0])
                .matches(messageEmbed -> messageEmbed.getDescription() == null)
                .matches(messageEmbed -> messageEmbed.getTitle().equals(title))
                .matches(messageEmbed -> messageEmbed.getFields().isEmpty())
                .matches(messageEmbed -> messageEmbed.getAuthor() == null);
    }

    @ParameterizedTest
    @ValueSource(ints = {MultiEmbedBuilder.EMBED_DESCRIPTION_MAX, MultiEmbedBuilder.EMBED_DESCRIPTION_MAX + 1, 1, 10_000, 100_000})
    void descriptions(final int totalSize) {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder()
                .setDescription(getRandomString(totalSize));

        AssertionsForClassTypes.assertThat(embedBuilder.build().toArray(new MessageEmbed[0]))
                .hasSize((int) Math.ceil((float) totalSize / MultiEmbedBuilder.EMBED_DESCRIPTION_MAX))
                .matches(messageEmbeds -> {
                    int totalDescriptionSize = 0;
                    for (final MessageEmbed messageEmbed : messageEmbeds) {
                        if (messageEmbed.getDescription() != null) {
                            totalDescriptionSize += messageEmbed.getDescription().length();
                        }
                    }

                    return totalSize == totalDescriptionSize;
                }, "Total Description Size")
                .matches(new TotalSizePredicate(), "Total size")
                .matches(new OrderPredicate(), "Order");
    }

    @ParameterizedTest
    @ValueSource(strings = {"loremipsum/20k", "loremipsum/5k", "loremipsum/111k"})
    void correctDescriptions(final String filePath) {
        final String description = getContentFromFile(filePath);

        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setDescription(description);

        assertThat(embedBuilder.build().toArray(new MessageEmbed[0]))
                .matches(new TotalSizePredicate(), "Total size")
                .matches(new OrderPredicate(), "Order")
                .matches(messageEmbeds -> {
                    final StringBuilder foundDescription = new StringBuilder(description.length());
                    for (final MessageEmbed messageEmbed : messageEmbeds) {
                        foundDescription.append(messageEmbed.getDescription());
                    }

                    return foundDescription.toString().equals(description);
                }, "Input != Output description");
    }

    @ParameterizedTest
    @ValueSource(ints = {MultiEmbedBuilder.EMBED_FIELD_MAX, MultiEmbedBuilder.EMBED_FIELD_MAX + 1, 1, 100, 200})
    void fields(final int fieldCount) {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        for (int index = 0; fieldCount > index; index++) {
            embedBuilder.addField(String.valueOf(index), String.valueOf(index));
        }

        assertThat(embedBuilder.build().toArray(new MessageEmbed[0]))
                .hasSize((int) Math.ceil((float) fieldCount / MultiEmbedBuilder.EMBED_FIELD_MAX))
                .matches(messageEmbeds -> {
                    int fields = 0;
                    for (final MessageEmbed messageEmbed : messageEmbeds) {
                        fields += messageEmbed.getFields().size();
                    }

                    return fieldCount == fields;
                }, "Total Fields Count")
                .matches(new TotalSizePredicate(), "Total size")
                .matches(new OrderPredicate(), "Order");
    }

    @ParameterizedTest
    @ValueSource(ints = {MultiEmbedBuilder.EMBED_FIELD_MAX, MultiEmbedBuilder.EMBED_FIELD_MAX + 1, 1, 100, 200})
    void correctFields(final int fieldCount) {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();

        final List<Pair<String, String>> values = new ArrayList<>();
        for (int count = 0; fieldCount > count; count++) {
            final String name = getRandomString(MultiEmbedBuilder.EMBED_FIELD_NAME_MAX);
            final String value = getRandomString(MultiEmbedBuilder.EMBED_FIELD_VALUE_MAX);

            values.add(Pair.of(name, value));
            embedBuilder.addField(name, value);
        }

        assertThat(embedBuilder.build().toArray(new MessageEmbed[0]))
                .matches(messageEmbeds -> {
                    int fields = 0;
                    for (final MessageEmbed messageEmbed : messageEmbeds) {
                        fields += messageEmbed.getFields().size();
                    }

                    return values.size() == fields;
                }, "Total Fields Count")
                .matches(messageEmbeds -> {
                    int count = 0;
                    for (final MessageEmbed messageEmbed : messageEmbeds) {
                        for (final MessageEmbed.Field field : messageEmbed.getFields()) {
                            final Pair<String, String> input = values.get(count);
                            if (!field.getName().equals(input.getLeft()) || !field.getValue().equals(input.getRight())) {
                                return false;
                            }
                            count++;
                        }
                    }
                    return true;
                }, "Input != Output")
                .matches(new TotalSizePredicate(), "Total size")
                .matches(new OrderPredicate(), "Order");
    }

    @Test
    void constructorMultiEmbedBuilder() {
        final MultiEmbedBuilder original = getFilledEmbedBuilder();
        final MultiEmbedBuilder copy = new MultiEmbedBuilder(original);

        assertThat(copy).isEqualTo(original);
    }

    @Test
    void constructorMessageEmbed() {
        final MultiEmbedBuilder original = getFilledEmbedBuilder();
        final MultiEmbedBuilder copy = new MultiEmbedBuilder(original.buildSingle());

        assertThat(copy).isEqualTo(original);
    }

    @Test
    void clear() {
        final MultiEmbedBuilder filledEmbedBuilder = getFilledEmbedBuilder();
        filledEmbedBuilder.clear();
        assertThat(filledEmbedBuilder).isEqualTo(new MultiEmbedBuilder());
    }

    @Test
    void clearFields() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder()
                .addField("D", "A")
                .addField("C", "f");
        embedBuilder.clearFields();
        assertThat(embedBuilder.getFields()).isEmpty();
    }

    @Test
    void addBlankField() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder().addBlankField(true);
        assertThat(embedBuilder.getFields()).hasSize(1);
    }

    @Test
    void addFieldField() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        final MessageEmbed.Field field = new MessageEmbed.Field("Name", "Value", true);
        embedBuilder.addField(field);
        assertThat(embedBuilder.getFields()).hasSize(1);
        assertThat(embedBuilder.getFields().get(0)).isEqualTo(field);
    }

    @Test
    void addFieldIfConditionFalse() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.addField("A", "A", true, false);
        assertThat(embedBuilder.getFields()).isEmpty();
    }

    @Test
    void addFieldIfConditionTrue() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.addField("A", "A", false, true);
        assertThat(embedBuilder.getFields()).hasSize(1);
    }

    @Test
    void setFooterNull() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setFooter(null);
        assertThat(embedBuilder.getFooter()).isNull();
    }

    @Test
    void setFooter() {
        final String footerText = "AAAAAAA";
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setFooter(footerText);

        assertThat(embedBuilder.getFooter()).isNotNull();
        assertThat(embedBuilder.getFooter().getText()).isEqualTo(footerText);
    }

    @Test
    void setFooterFormat() {
        final String format = "%s,%s,%s";
        final Object[] values = {"1", 2, "3"};

        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setFooterFormat(format, values);

        assertThat(embedBuilder.getFooter()).isNotNull();
        assertThat(embedBuilder.getFooter().getText()).isEqualTo(String.format(format, values));
    }

    @Test
    void setAuthorNull() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setAuthor(null);
        assertThat(embedBuilder.getAuthor()).isNull();
    }

    @Test
    void setAuthor() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setAuthor(null);
        assertThat(embedBuilder.getAuthor()).isNull();
    }

    @Test
    void setAuthor_name_and_url() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setAuthor(null, null);
        assertThat(embedBuilder.getAuthor()).isNull();
    }

    @Test
    void setThumbnail_null() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setThumbnail(null);
        assertThat(embedBuilder.getThumbnail()).isNull();
    }

    @Test
    void setThumbnail() {
        final String url = "https://i.imgur.com/UpnCDgA.jpg";
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setThumbnail(url);

        assertThat(embedBuilder.getThumbnail()).isNotNull();
        assertThat(embedBuilder.getThumbnail().getUrl()).isEqualTo(url);
    }

    @Test
    void setImage_null() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setImage(null);
        assertThat(embedBuilder.getImage()).isNull();
    }

    @Test
    void setImage() {
        final String url = "https://i.imgur.com/UpnCDgA.jpg";
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setImage(url);

        assertThat(embedBuilder.getImage()).isNotNull();
        assertThat(embedBuilder.getImage().getUrl()).isEqualTo(url);
    }

    @Test
    void setTitle_null() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setTitle(null);
        assertThat(embedBuilder.getTitle()).isNull();
    }

    @Test
    void setTitleFormat() {
        final String format = "Test %s,%s,%s";
        final Object[] objects = {1, 2, 3};

        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setTitleFormat(format, objects);
        assertThat(embedBuilder.getTitle()).isEqualTo(String.format(format, objects));
    }

    @Test
    void appendDescription_format() {
        final String format = "Test %s,%s,%s";
        final Object[] objects = {1, 2, 3};

        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.appendDescription(format, objects);
        assertThat(embedBuilder.getDescription()).isEqualTo(String.format(format, objects));
    }

    @Test
    void setDescription_format() {
        final String format = "Test %s,%s,%s";
        final Object[] objects = {1, 2, 3};

        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setDescription(format, objects);
        assertThat(embedBuilder.getDescription()).isEqualTo(String.format(format, objects));
    }

    @Test
    void setTimestamp_null() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setTimestamp(null);
        assertThat(embedBuilder.getTimestamp()).isNull();
    }

    @Test
    void setTimestamp_offsetDateTime() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        final OffsetDateTime dateTime = OffsetDateTime.now();
        embedBuilder.setTimestamp(dateTime);
        assertThat(embedBuilder.getTimestamp()).isEqualTo(dateTime);
    }

    @Test
    void setTimestamp_instant() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        final Instant dateTime = Instant.now();
        embedBuilder.setTimestamp(dateTime);
        assertThat(embedBuilder.getTimestamp()).isEqualTo(dateTime.toString());
    }

    @Test
    void buildSingle_empty() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        assertThrows(IllegalStateException.class, embedBuilder::buildSingle);
    }

    @Test
    void buildSingle_description_over_limit() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();
        embedBuilder.setDescription(getRandomString(MultiEmbedBuilder.EMBED_DESCRIPTION_MAX + 1));

        assertThrows(IllegalStateException.class, embedBuilder::buildSingle);
    }

    @Test
    void buildSingle_total_over_limit() {
        final MultiEmbedBuilder embedBuilder = new MultiEmbedBuilder();

        final String content = getRandomString(MultiEmbedBuilder.EMBED_FIELD_VALUE_MAX);
        final double fields = Math.floor((double) MultiEmbedBuilder.EMBED_TOTAL_MAX / MultiEmbedBuilder.EMBED_FIELD_VALUE_MAX);
        for (int count = 0; fields >= count; count++) {
            embedBuilder.addField("", content);
        }

        assertThrows(IllegalStateException.class, embedBuilder::buildSingle);
    }

    private static class TotalSizePredicate implements Predicate<MessageEmbed[]> {
        @Override
        public boolean test(final MessageEmbed[] messageEmbeds) {
            // Title, Description, Field, Footer.text, Author.name
            for (final MessageEmbed messageEmbed : messageEmbeds) {
                int size = 0;
                if (messageEmbed.getTitle() != null) {
                    if (messageEmbed.getTitle().length() > MultiEmbedBuilder.EMBED_TITLE_MAX) {
                        return false;
                    }

                    size += messageEmbed.getTitle().length();
                }

                if (messageEmbed.getDescription() != null) {
                    if (messageEmbed.getDescription().length() > MultiEmbedBuilder.EMBED_DESCRIPTION_MAX) {
                        return false;
                    }

                    size += messageEmbed.getDescription().length();
                }

                if (messageEmbed.getAuthor() != null && messageEmbed.getAuthor().getName() != null) {
                    if (messageEmbed.getAuthor().getName().length() > MultiEmbedBuilder.EMBED_AUTHOR_MAX) {
                        return false;
                    }

                    size += messageEmbed.getAuthor().getName().length();
                }

                for (final MessageEmbed.Field field : messageEmbed.getFields()) {
                    if (field.getName() != null) {
                        if (field.getName().length() > MultiEmbedBuilder.EMBED_FIELD_NAME_MAX) {
                            return false;
                        }

                        size += field.getName().length();
                    }

                    if (field.getValue() != null) {
                        if (field.getValue().length() > MultiEmbedBuilder.EMBED_FIELD_VALUE_MAX) {
                            return false;
                        }

                        size += field.getValue().length();
                    }
                }

                if (messageEmbed.getFooter() != null && messageEmbed.getFooter().getText() != null) {
                    if (messageEmbed.getFooter().getText().length() > MultiEmbedBuilder.EMBED_FOOTER_MAX) {
                        return false;
                    }

                    size += messageEmbed.getFooter().getText().length();
                }

                if (size > MultiEmbedBuilder.EMBED_TOTAL_MAX) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class OrderPredicate implements Predicate<MessageEmbed[]> {
        @Override
        public boolean test(final MessageEmbed[] messageEmbeds) {
            for (int index = 0; messageEmbeds.length > index; index++) {
                final MessageEmbed messageEmbed = messageEmbeds[index];

                if (index != 0 && index != messageEmbeds.length - 1) {
                    if (messageEmbed.getTitle() != null || messageEmbed.getAuthor() != null || messageEmbed.getFooter() != null || messageEmbed.getTimestamp() != null) {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}