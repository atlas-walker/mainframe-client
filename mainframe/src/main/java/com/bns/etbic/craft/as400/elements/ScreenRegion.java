package com.bns.etbic.craft.as400.elements;

import java.util.regex.Pattern;

/**
 * Immutable rectangular slice of screen text: its position, length and the text it
 * held when captured. Returned by text locators and used for assertions.
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public final class ScreenRegion {

    private final int row;
    private final int col;
    private final int length;
    private final String text;

    /**
     * Creates a region.
     *
     * @param row    the 1-based row
     * @param col    the 1-based starting column
     * @param length the length in characters
     * @param text   the captured text
     */
    public ScreenRegion(int row, int col, int length, String text) {
        this.row = row;
        this.col = col;
        this.length = length;
        this.text = text;
    }

    /**
     * Returns the region row.
     *
     * @return the 1-based row
     */
    public int row()      { return row; }

    /**
     * Returns the region's starting column.
     *
     * @return the 1-based starting column
     */
    public int col()      { return col; }

    /**
     * Returns the region length.
     *
     * @return the length, in characters
     */
    public int length()   { return length; }

    /**
     * Returns the captured text.
     *
     * @return the captured text
     */
    public String text()  { return text; }

    /**
     * Tests whether the captured text matches the given regular expression.
     *
     * @param regex the regular expression
     * @return {@code true} if the whole text matches
     */
    public boolean matches(String regex) {
        return Pattern.compile(regex).matcher(text).matches();
    }

    /**
     * Tests whether the captured text matches the given compiled pattern.
     *
     * @param pattern the compiled pattern
     * @return {@code true} if the whole text matches
     */
    public boolean matches(Pattern pattern) {
        return pattern.matcher(text).matches();
    }

    @Override
    public String toString() {
        return "ScreenRegion[row=" + row + ", col=" + col + ", len=" + length
            + ", text=\"" + text + "\"]";
    }
}
