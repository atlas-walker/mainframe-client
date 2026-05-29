package com.bns.etbic.craft.as400.elements;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenOIA;

/**
 * Immutable snapshot of a 5250 screen at a single instant: its geometry, cursor
 * position, keyboard/input state and the text, color and extended-attribute planes.
 *
 * <p>Because it is a frozen copy, assertions made against a snapshot are stable even
 * if the host repaints afterwards. Obtain one with {@link #take(Screen5250)} (the
 * driver does this internally for {@code getScreen()} and the wait loops).
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public final class ScreenSnapshot {

    private final int rows;
    private final int cols;
    private final int cursorRow;
    private final int cursorCol;
    private final int oiaInputInhibited;
    private final boolean keyboardLocked;
    private final char[] text;
    private final char[] color;
    private final char[] extended;
    private final long timestamp;

    private ScreenSnapshot(int rows, int cols, int cursorRow, int cursorCol,
                           int oiaInputInhibited, boolean keyboardLocked,
                           char[] text, char[] color, char[] extended, long timestamp) {
        this.rows = rows;
        this.cols = cols;
        this.cursorRow = cursorRow;
        this.cursorCol = cursorCol;
        this.oiaInputInhibited = oiaInputInhibited;
        this.keyboardLocked = keyboardLocked;
        this.text = text;
        this.color = color;
        this.extended = extended;
        this.timestamp = timestamp;
    }

    /**
     * Captures the current state of the given emulator screen.
     *
     * @param screen the live emulator screen
     * @return an immutable snapshot of it
     */
    public static ScreenSnapshot take(Screen5250 screen) {
        int rows = screen.getRows();
        int cols = screen.getColumns();
        ScreenOIA oia = screen.getOIA();
        char[] text = screen.getData(0, 0, rows - 1, cols - 1, TN5250jConstants.PLANE_TEXT);
        char[] color = screen.getData(0, 0, rows - 1, cols - 1, TN5250jConstants.PLANE_COLOR);
        char[] extended = screen.getData(0, 0, rows - 1, cols - 1, TN5250jConstants.PLANE_EXTENDED);
        return new ScreenSnapshot(
            rows, cols,
            screen.getCurrentRow(), screen.getCurrentCol(),
            oia.getInputInhibited(), oia.isKeyBoardLocked(),
            text, color, extended,
            System.currentTimeMillis()
        );
    }

    /**
     * Returns the screen height.
     *
     * @return the number of rows
     */
    public int rows()           { return rows; }

    /**
     * Returns the screen width.
     *
     * @return the number of columns
     */
    public int cols()           { return cols; }

    /**
     * Returns the cursor row at capture time.
     *
     * @return the 1-based cursor row
     */
    public int cursorRow()      { return cursorRow; }

    /**
     * Returns the cursor column at capture time.
     *
     * @return the 1-based cursor column
     */
    public int cursorCol()      { return cursorCol; }

    /**
     * Returns when the snapshot was taken.
     *
     * @return the capture time, in epoch milliseconds
     */
    public long timestamp()     { return timestamp; }

    /**
     * Tells whether the keyboard was locked at capture time.
     *
     * @return {@code true} if the keyboard was locked
     */
    public boolean keyboardLocked() { return keyboardLocked; }

    /**
     * Tells whether the host is ready to accept keystrokes.
     *
     * @return {@code true} if input is not inhibited and the keyboard is unlocked
     */
    public boolean inputReady() {
        return oiaInputInhibited == ScreenOIA.INPUTINHIBITED_NOTINHIBITED && !keyboardLocked;
    }

    /**
     * Returns the character at a position, mapping nulls to spaces.
     *
     * @param row1Based the 1-based row
     * @param col1Based the 1-based column
     * @return the character at that position
     */
    public char charAt(int row1Based, int col1Based) {
        int idx = index(row1Based, col1Based);
        char ch = text[idx];
        return ch == 0 ? ' ' : ch;
    }

    /**
     * Returns the color attribute byte at a position.
     *
     * @param row1Based the 1-based row
     * @param col1Based the 1-based column
     * @return the color attribute byte
     */
    public int colorAt(int row1Based, int col1Based) {
        return color[index(row1Based, col1Based)];
    }

    /**
     * Returns the extended attribute byte at a position.
     *
     * @param row1Based the 1-based row
     * @param col1Based the 1-based column
     * @return the extended attribute byte
     */
    public int extendedAt(int row1Based, int col1Based) {
        return extended[index(row1Based, col1Based)];
    }

    /**
     * Returns the full text of a row, with nulls rendered as spaces.
     *
     * @param row1Based the 1-based row
     * @return the row text
     */
    public String row(int row1Based) {
        StringBuilder sb = new StringBuilder(cols);
        int base = (row1Based - 1) * cols;
        for (int c = 0; c < cols; c++) {
            char ch = text[base + c];
            sb.append(ch == 0 ? ' ' : ch);
        }
        return sb.toString();
    }

    /**
     * Returns every row of the screen.
     *
     * @return the rows as a string array
     */
    public String[] allRows() {
        String[] out = new String[rows];
        for (int r = 1; r <= rows; r++) {
            out[r - 1] = row(r);
        }
        return out;
    }

    /**
     * Returns the whole screen as text.
     *
     * @return the screen as a single newline-separated string
     */
    public String allText() {
        StringBuilder sb = new StringBuilder(rows * (cols + 1));
        for (int r = 1; r <= rows; r++) {
            sb.append(row(r));
            if (r < rows) sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Returns a fixed-length substring of the screen starting at a position.
     *
     * @param row1Based the 1-based row
     * @param col1Based the 1-based starting column
     * @param length    the number of characters to read
     * @return the text at that position
     */
    public String textAt(int row1Based, int col1Based, int length) {
        StringBuilder sb = new StringBuilder(length);
        int base = (row1Based - 1) * cols + (col1Based - 1);
        for (int i = 0; i < length && base + i < text.length; i++) {
            char ch = text[base + i];
            sb.append(ch == 0 ? ' ' : ch);
        }
        return sb.toString();
    }

    /**
     * Tells whether the whole screen text contains the given needle.
     *
     * @param needle the text to look for ({@code null} or empty matches)
     * @return {@code true} if the screen contains {@code needle}
     */
    public boolean contains(String needle) {
        if (needle == null || needle.isEmpty()) return true;
        return allText().contains(needle);
    }

    /**
     * Returns the raw text plane.
     *
     * @return the text plane (row-major, may contain nulls)
     */
    public char[] textPlane()     { return text; }

    /**
     * Returns the raw color-attribute plane.
     *
     * @return the color plane (row-major)
     */
    public char[] colorPlane()    { return color; }

    /**
     * Returns the raw extended-attribute plane.
     *
     * @return the extended-attribute plane (row-major)
     */
    public char[] extendedPlane() { return extended; }

    private int index(int row1Based, int col1Based) {
        if (row1Based < 1 || row1Based > rows || col1Based < 1 || col1Based > cols) {
            throw new IndexOutOfBoundsException(
                "row/col out of range: (" + row1Based + "," + col1Based + ") on " + rows + "x" + cols);
        }
        return (row1Based - 1) * cols + (col1Based - 1);
    }
}
