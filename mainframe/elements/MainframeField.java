package org.tn5250j.mainframe.elements;

import org.tn5250j.framework.tn5250.ScreenField;

public final class MainframeField {

    private final ScreenField field;
    private final FieldActions actions;

    public MainframeField(ScreenField field, FieldActions actions) {
        this.field = field;
        this.actions = actions;
    }

    public int row() {
        return field.startRow() + 1;
    }

    public int col() {
        return field.startCol() + 1;
    }

    public int length() {
        return field.getLength();
    }

    public boolean isProtected() {
        return field.isBypassField();
    }

    public boolean isNumeric() {
        return field.isNumeric();
    }

    public boolean isToUpper() {
        return field.isToUpper();
    }

    public boolean isMandatoryEnter() {
        return field.isMandatoryEnter();
    }

    public boolean isAutoEnter() {
        return field.isAutoEnter();
    }

    public String getText() {
        return field.getString();
    }

    public MainframeField type(String text) {
        actions.typeAt(row(), col(), text);
        return this;
    }

    public MainframeField clear() {
        actions.clearField(row(), col(), length());
        return this;
    }

    public ScreenField raw() {
        return field;
    }

    @Override
    public String toString() {
        return "MainframeField[row=" + row() + ", col=" + col()
            + ", len=" + length()
            + (isProtected() ? ", protected" : "")
            + "]";
    }
}
