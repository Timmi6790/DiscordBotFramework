package de.timmi6790.statsbotdiscord.modules.mineplexstats;

import de.timmi6790.statsbotdiscord.utilities.UtilitiesArray;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@ToString
@EqualsAndHashCode
public class PictureTable {
    private static final Font FONT_HEADER = new Font("Arial", Font.PLAIN, 42);
    private static final Font FONT_SUB_HEADER = new Font("Arial", Font.PLAIN, 33);

    private static final Font FONT_LEADERBOARD_HEADER = new Font("Arial", Font.PLAIN, 38);
    private static final Font FONT_LEADERBOARD = new Font("Arial", Font.PLAIN, 30);

    private final static int GAP_X_BORDER = 10;
    private final static int GAP_Y_ROW = 15;
    private final static int GAP_WORD_MIN = 20;

    private final static int GAP_HEADER = GAP_Y_ROW / 2;
    private final static int GAP_SUB_HEADER = (int) (GAP_Y_ROW * 2.3);
    private final static int GAP_LEADERBOARD_HEADER = GAP_Y_ROW * 2;

    private final static Color COLOUR_DISCORD_DARK_MODE = new Color(54, 57, 63);

    private static final Graphics2D GD_TEST = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR).createGraphics();

    private final String[] header;
    private final String[][] leaderboard;
    private final String date;

    private final BufferedImage skin;
    private int skinX = 0;
    private int skinY = 0;

    private int maxWidth = 0;
    private int maxHeight = 0;

    private int widthHeaderMax = 0;
    private int widthDateMax = 0;
    private int widthLeaderboardMax = 0;

    private final int[] widthHeader;
    private final int[] widthLeaderboard;

    private int currentHeightY = FONT_HEADER.getSize();
    private Graphics2D gd = null;

    public PictureTable(final String[] header, final String date, final String[][] leaderboard, final BufferedImage skin) {
        this.header = header;
        this.leaderboard = leaderboard;
        this.date = date;

        this.widthHeader = new int[this.header.length];
        this.widthLeaderboard = new int[this.leaderboard[0].length];

        this.skin = skin;
    }

    public PictureTable(final String[] header, final String date, final String[][] leaderboard) {
        this.header = header;
        this.leaderboard = leaderboard;
        this.date = date;

        this.widthHeader = new int[this.header.length];
        this.widthLeaderboard = new int[this.leaderboard[0].length];

        this.skin = null;
    }

    private static int getTextWidth(final String text, final Font font) {
        return PictureTable.GD_TEST.getFontMetrics(font).stringWidth(text);
    }

    public Optional<InputStream> getPlayerPicture() {
        this.calculateImageDimension();

        final BufferedImage image = new BufferedImage(this.maxWidth, this.maxHeight, BufferedImage.TYPE_4BYTE_ABGR);
        this.gd = image.createGraphics();

        // Background
        this.gd.setPaint(COLOUR_DISCORD_DARK_MODE);
        this.gd.fillRect(0, 0, this.maxWidth, this.maxHeight);
        this.gd.setPaint(Color.WHITE);

        // Header, center if only one entry
        if (this.header.length <= 1) {
            this.gd.setFont(FONT_HEADER);
            this.gd.drawString(this.header[0], GAP_X_BORDER + (Math.max(this.widthDateMax, this.widthLeaderboardMax) - getTextWidth(this.header[0], FONT_HEADER)) / 2, this.currentHeightY);
            this.currentHeightY += GAP_HEADER + FONT_HEADER.getSize();

        } else {
            final int distanceWord = Math.max(((Math.max(this.widthHeaderMax, this.widthLeaderboardMax) - UtilitiesArray.getSum(this.widthHeader)) / (this.header.length - 1)), GAP_WORD_MIN);
            this.drawRow(this.header, this.widthHeader, FONT_HEADER, distanceWord, GAP_HEADER + FONT_SUB_HEADER.getSize());
        }

        // Sub header
        this.gd.setFont(FONT_SUB_HEADER);
        this.gd.drawString(this.date, GAP_X_BORDER + (Math.max(this.widthDateMax, this.widthLeaderboardMax) - getTextWidth(this.date, FONT_SUB_HEADER)) / 2, this.currentHeightY);
        this.currentHeightY += GAP_SUB_HEADER + FONT_LEADERBOARD_HEADER.getSize();

        // Leaderboard header centered above leaderboard data
        this.drawRow(this.leaderboard[0], this.widthLeaderboard, FONT_LEADERBOARD_HEADER, GAP_WORD_MIN, GAP_LEADERBOARD_HEADER + FONT_LEADERBOARD.getSize());

        // Leaderboard
        for (int columnIndex = 1; this.leaderboard.length > columnIndex; columnIndex++) {
            this.drawRow(this.leaderboard[columnIndex], this.widthLeaderboard, FONT_LEADERBOARD, GAP_WORD_MIN, FONT_LEADERBOARD.getSize() + GAP_Y_ROW);
        }

        // Skin
        if (this.skin != null) {
            this.gd.drawImage(this.skin, this.skinX, this.skinY, null);
        }

        this.gd.dispose();

        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", os);
            return Optional.of(new ByteArrayInputStream(os.toByteArray()));
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private void calculateImageDimension() {
        // Header width
        for (int index = 0; this.widthHeader.length > index; index++) {
            this.widthHeader[index] = getTextWidth(this.header[index], FONT_HEADER);
        }

        // Leaderboard width
        for (int columnIndex = 0; this.widthLeaderboard.length > columnIndex; columnIndex++) {
            for (int rowIndex = 0; this.leaderboard.length > rowIndex; rowIndex++) {
                final int width = getTextWidth(this.leaderboard[rowIndex][columnIndex], rowIndex == 0 ? FONT_LEADERBOARD_HEADER : FONT_LEADERBOARD);

                if (width > this.widthLeaderboard[columnIndex]) {
                    this.widthLeaderboard[columnIndex] = width;
                }
            }
        }

        // Width
        this.widthHeaderMax = UtilitiesArray.getSum(this.widthHeader) + GAP_WORD_MIN * (this.widthHeader.length - 1);
        this.widthDateMax = getTextWidth(this.date, FONT_SUB_HEADER) + GAP_WORD_MIN;
        int widthLeaderboardMax = UtilitiesArray.getSum(this.widthLeaderboard) + GAP_WORD_MIN * (this.widthLeaderboard.length - 1);

        this.widthLeaderboardMax = widthLeaderboardMax;

        // Height
        final int heightHeader = FONT_HEADER.getSize() + GAP_HEADER;
        final int heightSubHeader = FONT_SUB_HEADER.getSize() + GAP_SUB_HEADER;
        final int heightLeaderboardHeader = FONT_LEADERBOARD_HEADER.getSize() + GAP_LEADERBOARD_HEADER + GAP_Y_ROW;
        int heightLeaderboard = (this.leaderboard.length - 1) * FONT_LEADERBOARD.getSize() + (this.leaderboard.length - 1) * GAP_Y_ROW;

        // If a skin is found, we place it directly next to the leaderboard
        if (this.skin != null) {
            this.skinX = widthLeaderboardMax;
            widthLeaderboardMax += this.skin.getWidth();

            this.skinY = heightHeader + heightSubHeader + heightLeaderboardHeader + 2;
            if (this.skin.getHeight() > heightLeaderboard) {
                heightLeaderboard = this.skin.getHeight();
            }
        }

        this.maxWidth = Math.max(Math.max(this.widthHeaderMax, this.widthDateMax), widthLeaderboardMax) + GAP_X_BORDER * 2;
        this.maxHeight = heightHeader + heightSubHeader + heightLeaderboardHeader + heightLeaderboard + GAP_Y_ROW;
    }

    private void drawRow(final String[] dataArray, final int[] widthArray, final Font font, final int increaseX, final int rowHeight) {
        this.gd.setFont(font);
        for (int index = 0, xPos = GAP_X_BORDER; dataArray.length > index; xPos += widthArray[index] + increaseX, index++) {
            this.gd.drawString(dataArray[index], xPos, this.currentHeightY);
        }
        this.currentHeightY += rowHeight;
    }
}
