package com.bns.etbic.craft.as400.elements;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenOIA;

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

    public int rows()           { return rows; }
    public int cols()           { return cols; }
    public int cursorRow()      { return cursorRow; }
    public int cursorCol()      { return cursorCol; }
    public long timestamp()     { return timestamp; }
    public boolean keyboardLocked() { return keyboardLocked; }

    public boolean inputReady() {
        return oiaInputInhibited == ScreenOIA.INPUTINHIBITED_NOTINHIBITED && !keyboardLocked;
    }

    public char charAt(int row1Based, int col1Based) {
        int idx = index(row1Based, col1Based);
        char ch = text[idx];
        return ch == 0 ? ' ' : ch;
    }

    public int colorAt(int row1Based, int col1Based) {
        return color[index(row1Based, col1Based)];
    }

    public int extendedAt(int row1Based, int col1Based) {
        return extended[index(row1Based, col1Based)];
    }

    public String row(int row1Based) {
        StringBuilder sb = new StringBuilder(cols);
        int base = (row1Based - 1) * cols;
        for (int c = 0; c < cols; c++) {
            char ch = text[base + c];
            sb.append(ch == 0 ? ' ' : ch);
        }
        return sb.toString();
    }

    public String[] allRows() {
        String[] out = new String[rows];
        for (int r = 1; r <= rows; r++) {
            out[r - 1] = row(r);
        }
        return out;
    }

    public String allText() {
        StringBuilder sb = new StringBuilder(rows * (cols + 1));
        for (int r = 1; r <= rows; r++) {
            sb.append(row(r));
            if (r < rows) sb.append('\n');
        }
        return sb.toString();
    }

    public String textAt(int row1Based, int col1Based, int length) {
        StringBuilder sb = new StringBuilder(length);
        int base = (row1Based - 1) * cols + (col1Based - 1);
        for (int i = 0; i < length && base + i < text.length; i++) {
            char ch = text[base + i];
            sb.append(ch == 0 ? ' ' : ch);
        }
        return sb.toString();
    }

    public boolean contains(String needle) {
        if (needle == null || needle.isEmpty()) return true;
        return allText().contains(needle);
    }

    public char[] textPlane()     { return text; }
    public char[] colorPlane()    { return color; }
    public char[] extendedPlane() { return extended; }

    private int index(int row1Based, int col1Based) {
        if (row1Based < 1 || row1Based > rows || col1Based < 1 || col1Based > cols) {
            throw new IndexOutOfBoundsException(
                "row/col out of range: (" + row1Based + "," + col1Based + ") on " + rows + "x" + cols);
        }
        return (row1Based - 1) * cols + (col1Based - 1);
    }
}
