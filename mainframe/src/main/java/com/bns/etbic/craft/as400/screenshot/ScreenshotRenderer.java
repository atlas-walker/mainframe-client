package com.bns.etbic.craft.as400.screenshot;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.tn5250j.TN5250jConstants;
import com.bns.etbic.craft.as400.elements.ScreenSnapshot;

/**
 * Renders a {@link ScreenSnapshot} into a {@link BufferedImage} using a monospaced
 * font, honoring 5250 color, reverse-video, underline and non-display attributes and
 * optionally drawing the cursor.
 *
 * <p>The image resolution is driven by the font size: a larger font yields a larger,
 * sharper screenshot. A uniform {@linkplain #ScreenshotRenderer(ColorPalette, int,
 * boolean, int) padding} margin is drawn around the screen so the text never sits
 * flush against the edges — this keeps the last row readable when a report viewer
 * (for example Extent) scales the image or wraps it in a bordered container.
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public final class ScreenshotRenderer {

    /** Default font size, chosen for a crisp, HD-sized screenshot. */
    private static final int DEFAULT_FONT_SIZE = 24;

    /** Default margin, in pixels, drawn around the screen content. */
    private static final int DEFAULT_PADDING = 12;

    private final ColorPalette palette;
    private final int fontSize;
    private final int padding;
    private final int cellWidth;
    private final int cellHeight;
    private final boolean drawCursor;

    /** Creates a renderer with the default palette, font size, padding and the cursor drawn. */
    public ScreenshotRenderer() {
        this(ColorPalette.defaultPalette(), DEFAULT_FONT_SIZE);
    }

    /**
     * Creates a renderer with the default padding and the cursor drawn.
     *
     * @param palette  the color palette
     * @param fontSize the monospaced font size, in points
     */
    public ScreenshotRenderer(ColorPalette palette, int fontSize) {
        this(palette, fontSize, true);
    }

    /**
     * Creates a renderer with the default padding.
     *
     * @param palette    the color palette
     * @param fontSize   the monospaced font size, in points
     * @param drawCursor whether to draw the cursor position
     */
    public ScreenshotRenderer(ColorPalette palette, int fontSize, boolean drawCursor) {
        this(palette, fontSize, drawCursor, DEFAULT_PADDING);
    }

    /**
     * Creates a renderer with full control over its settings.
     *
     * @param palette    the color palette
     * @param fontSize   the monospaced font size, in points (drives the resolution)
     * @param drawCursor whether to draw the cursor position
     * @param padding    the margin, in pixels, drawn around the screen content
     */
    public ScreenshotRenderer(ColorPalette palette, int fontSize, boolean drawCursor, int padding) {
        this.palette = palette;
        this.fontSize = fontSize;
        this.drawCursor = drawCursor;
        this.padding = Math.max(0, padding);

        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = probe.createGraphics();
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        FontMetrics fm = g.getFontMetrics();
        this.cellWidth = Math.max(fm.charWidth('M'), fm.charWidth('W'));
        this.cellHeight = fm.getHeight();
        g.dispose();
    }

    /**
     * Renders a screen snapshot into an image.
     *
     * @param snap the snapshot to render
     * @return the rendered image
     */
    public BufferedImage render(ScreenSnapshot snap) {
        int width = snap.cols() * cellWidth + 2 * padding;
        int height = snap.rows() * cellHeight + 2 * padding;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(palette.background());
        g.fillRect(0, 0, width, height);

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int ascent = fm.getAscent();

        for (int r = 1; r <= snap.rows(); r++) {
            int y = padding + (r - 1) * cellHeight;
            for (int c = 1; c <= snap.cols(); c++) {
                int x = padding + (c - 1) * cellWidth;
                drawCell(g, snap, r, c, x, y, ascent);
            }
        }

        if (drawCursor) {
            int cr = snap.cursorRow();
            int cc = snap.cursorCol();
            if (cr >= 1 && cr <= snap.rows() && cc >= 1 && cc <= snap.cols()) {
                int x = padding + (cc - 1) * cellWidth;
                int y = padding + (cr - 1) * cellHeight;
                g.setColor(palette.cursor());
                g.fillRect(x, y + cellHeight - 2, cellWidth, 2);
            }
        }

        g.dispose();
        return img;
    }

    private void drawCell(Graphics2D g, ScreenSnapshot snap, int r, int c, int x, int y, int ascent) {
        char ch = snap.charAt(r, c);
        int colorByte = snap.colorAt(r, c);
        int extended = snap.extendedAt(r, c);

        boolean reverse = (extended & TN5250jConstants.EXTENDED_5250_REVERSE) != 0;
        boolean underline = (extended & TN5250jConstants.EXTENDED_5250_UNDERLINE) != 0;
        boolean nonDisplay = (extended & TN5250jConstants.EXTENDED_5250_NON_DSP) != 0;

        Color fg = palette.foreground(colorByte);
        Color bg = palette.backgroundOf(colorByte);
        if (reverse) {
            Color tmp = fg; fg = bg; bg = tmp;
        }

        if (bg.getRGB() != palette.background().getRGB()) {
            g.setColor(bg);
            g.fillRect(x, y, cellWidth, cellHeight);
        }

        if (!nonDisplay && ch != 0 && ch != ' ') {
            g.setColor(fg);
            g.drawString(String.valueOf(ch), x, y + ascent);
        }

        if (underline) {
            g.setColor(fg);
            g.drawLine(x, y + cellHeight - 1, x + cellWidth, y + cellHeight - 1);
        }
    }

    /**
     * Returns the width of one character cell.
     *
     * @return the cell width, in pixels
     */
    public int cellWidth()  { return cellWidth; }

    /**
     * Returns the height of one character cell.
     *
     * @return the cell height, in pixels
     */
    public int cellHeight() { return cellHeight; }
}
