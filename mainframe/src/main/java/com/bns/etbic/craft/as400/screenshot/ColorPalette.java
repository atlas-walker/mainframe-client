package com.bns.etbic.craft.as400.screenshot;

import java.awt.Color;

import org.tn5250j.TN5250jConstants;

/**
 * Maps 5250 color attribute bytes to AWT {@link Color}s for screenshot rendering.
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public final class ColorPalette {

    private final Color background;
    private final Color foregroundDefault;
    private final Color blue;
    private final Color cyan;
    private final Color red;
    private final Color magenta;
    private final Color yellow;
    private final Color green;
    private final Color white;
    private final Color cursor;

    /**
     * Creates a palette from explicit colors.
     *
     * @param background        the screen background color
     * @param foregroundDefault the fallback foreground color
     * @param blue              the color for the 5250 blue attribute
     * @param cyan              the color for the 5250 cyan/turquoise attribute
     * @param red               the color for the 5250 red attribute
     * @param magenta           the color for the 5250 magenta/pink attribute
     * @param yellow            the color for the 5250 yellow attribute
     * @param green             the color for the 5250 green attribute
     * @param white             the color for the 5250 white attribute
     * @param cursor            the color used to draw the cursor
     */
    public ColorPalette(Color background, Color foregroundDefault,
                        Color blue, Color cyan, Color red, Color magenta,
                        Color yellow, Color green, Color white, Color cursor) {
        this.background = background;
        this.foregroundDefault = foregroundDefault;
        this.blue = blue;
        this.cyan = cyan;
        this.red = red;
        this.magenta = magenta;
        this.yellow = yellow;
        this.green = green;
        this.white = white;
        this.cursor = cursor;
    }

    /**
     * Returns a green-on-black palette resembling a classic 5250 terminal.
     *
     * @return the default palette
     */
    public static ColorPalette defaultPalette() {
        return new ColorPalette(
            Color.black,
            Color.green,
            new Color(140, 120, 255),
            new Color(0, 240, 255),
            Color.red,
            Color.magenta,
            Color.yellow,
            Color.green,
            Color.white,
            Color.white
        );
    }

    /**
     * Returns the screen background color.
     *
     * @return the background color
     */
    public Color background()        { return background; }

    /**
     * Returns the fallback foreground color.
     *
     * @return the default foreground color
     */
    public Color foregroundDefault() { return foregroundDefault; }

    /**
     * Returns the cursor color.
     *
     * @return the cursor color
     */
    public Color cursor()            { return cursor; }

    /**
     * Resolves the foreground color encoded in a 5250 color byte.
     *
     * @param colorByte the 5250 color attribute byte
     * @return the foreground color, or the default when unmapped
     */
    public Color foreground(int colorByte) {
        int idx = colorByte & 0x00FF;
        return mapIndex(idx, foregroundDefault);
    }

    /**
     * Resolves the background color encoded in a 5250 color byte.
     *
     * @param colorByte the 5250 color attribute byte
     * @return the background color, or the screen background when unmapped
     */
    public Color backgroundOf(int colorByte) {
        int idx = (colorByte & 0xFF00) >> 8;
        return mapIndex(idx, background);
    }

    private Color mapIndex(int idx, Color fallback) {
        switch (idx) {
            case TN5250jConstants.COLOR_FG_BLACK:   return background;
            case TN5250jConstants.COLOR_FG_BLUE:    return blue;
            case TN5250jConstants.COLOR_FG_GREEN:   return green;
            case TN5250jConstants.COLOR_FG_CYAN:    return cyan;
            case TN5250jConstants.COLOR_FG_RED:     return red;
            case TN5250jConstants.COLOR_FG_MAGENTA: return magenta;
            case TN5250jConstants.COLOR_FG_YELLOW:  return yellow;
            case TN5250jConstants.COLOR_FG_WHITE:   return white;
            default: return fallback;
        }
    }
}
