package de.timmi6790.statsbotdiscord.modules.mineplexstats.picture;

import lombok.Data;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

public class PicturePie extends AbstractPicture {
    private static final Color[] COLOURS = new Color[]{Color.BLACK, Color.MAGENTA, Color.CYAN, Color.red, Color.orange};
    private int lastColourId = 0;

    private final Slice[] slices;
    private final double total;

    public PicturePie(final Slice[] slices) {
        this.slices = slices.clone();
        this.total = Arrays.stream(this.slices).mapToDouble(Slice::getValue).sum();
    }

    private Color getNextColour() {
        if (this.lastColourId >= COLOURS.length) {
            this.lastColourId = 0;
        }

        return COLOURS[this.lastColourId++];
    }

    public Optional<InputStream> getPie() {
        final BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D gd = getDiscordGraphics(image);

        // Pie
        final Arc2D.Double arc = new Arc2D.Double(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight(), 0, 0, Arc2D.PIE);
        for (final Slice slice : this.slices) {
            final double angleExtend = slice.value / this.total * 360;
            arc.setAngleStart(arc.getAngleStart() + arc.getAngleExtent());
            arc.setAngleExtent(angleExtend);

            gd.setColor(this.getNextColour());
            gd.fill(arc);

            gd.setPaint(Color.WHITE);
            gd.drawLine((int) (arc.getStartPoint().getX() - arc.getEndPoint().getX()), (int) (arc.getStartPoint().getX() - arc.getEndPoint().getX()) + 2, (int) (arc.getStartPoint().getY() - arc.getEndPoint().getY()), (int) (arc.getStartPoint().getY() - arc.getEndPoint().getY()) + 2);
            System.out.println(arc.getStartPoint() + " " + arc.getEndPoint());
        }

        // Total text
        gd.setPaint(Color.WHITE);
        gd.setFont(new Font("Arial", Font.PLAIN, 42));
        gd.drawString(
                String.valueOf(this.total),
                image.getWidth() / 2 - (getTextWidth(String.valueOf(this.total), new Font("Arial", Font.PLAIN, 42)) / 2),
                image.getHeight() / 2
        );

        gd.dispose();
        return convertToInputStream(image);
    }

    @Data
    public static class Slice {
        private final String name;
        private final long value;
    }
}
