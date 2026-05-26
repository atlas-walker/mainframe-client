package com.bns.etbic.craft.as400.screenshot;

import java.awt.Color;

import org.tn5250j.TN5250jConstants;

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

    public Color background()        { return background; }
    public Color foregroundDefault() { return foregroundDefault; }
    public Color cursor()            { return cursor; }

    public Color foreground(int colorByte) {
        int idx = colorByte & 0x00FF;
        return mapIndex(idx, foregroundDefault);
    }

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
