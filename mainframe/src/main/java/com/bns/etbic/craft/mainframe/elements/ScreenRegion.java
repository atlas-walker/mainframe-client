package com.bns.etbic.craft.mainframe.elements;

import java.util.regex.Pattern;

public final class ScreenRegion {

    private final int row;
    private final int col;
    private final int length;
    private final String text;

    public ScreenRegion(int row, int col, int length, String text) {
        this.row = row;
        this.col = col;
        this.length = length;
        this.text = text;
    }

    public int row()      { return row; }
    public int col()      { return col; }
    public int length()   { return length; }
    public String text()  { return text; }

    public boolean matches(String regex) {
        return Pattern.compile(regex).matcher(text).matches();
    }

    public boolean matches(Pattern pattern) {
        return pattern.matcher(text).matches();
    }

    @Override
    public String toString() {
        return "ScreenRegion[row=" + row + ", col=" + col + ", len=" + length
            + ", text=\"" + text + "\"]";
    }
}
