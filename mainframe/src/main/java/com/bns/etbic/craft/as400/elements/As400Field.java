package com.bns.etbic.craft.as400.elements;

import org.tn5250j.framework.tn5250.ScreenField;

/**
 * An input field on the current 5250 screen. Wraps the emulator's
 * {@link ScreenField}, exposing its position and attributes and letting callers type
 * into or clear it through a {@link FieldActions} supplied by the driver.
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public final class As400Field {

    private final ScreenField field;
    private final FieldActions actions;

    /**
     * Wraps an emulator field together with the actions used to operate on it.
     *
     * @param field   the underlying emulator field
     * @param actions the actions used to type into or clear the field
     */
    public As400Field(ScreenField field, FieldActions actions) {
        this.field = field;
        this.actions = actions;
    }

    /**
     * Returns the row where the field starts.
     *
     * @return the 1-based start row
     */
    public int row() {
        return field.startRow() + 1;
    }

    /**
     * Returns the column where the field starts.
     *
     * @return the 1-based start column
     */
    public int col() {
        return field.startCol() + 1;
    }

    /**
     * Returns the field length.
     *
     * @return the length, in characters
     */
    public int length() {
        return field.getLength();
    }

    /**
     * Tells whether the field is protected.
     *
     * @return {@code true} if the field is protected (bypass) and cannot be typed into
     */
    public boolean isProtected() {
        return field.isBypassField();
    }

    /**
     * Tells whether the field accepts only numeric input.
     *
     * @return {@code true} if the field is numeric
     */
    public boolean isNumeric() {
        return field.isNumeric();
    }

    /**
     * Tells whether the field converts input to uppercase.
     *
     * @return {@code true} if the field uppercases input
     */
    public boolean isToUpper() {
        return field.isToUpper();
    }

    /**
     * Tells whether the field requires an explicit field exit.
     *
     * @return {@code true} if the field is mandatory-enter
     */
    public boolean isMandatoryEnter() {
        return field.isMandatoryEnter();
    }

    /**
     * Tells whether filling the field auto-advances.
     *
     * @return {@code true} if the field is auto-enter
     */
    public boolean isAutoEnter() {
        return field.isAutoEnter();
    }

    /**
     * Returns the field's current contents.
     *
     * @return the current text held by the field
     */
    public String getText() {
        return field.getString();
    }

    /**
     * Types text into the field at its starting position.
     *
     * @param text the text to type
     * @return this field, for chaining
     */
    public As400Field type(String text) {
        actions.typeAt(row(), col(), text);
        return this;
    }

    /**
     * Clears the field's contents.
     *
     * @return this field, for chaining
     */
    public As400Field clear() {
        actions.clearField(row(), col(), length());
        return this;
    }

    /**
     * Exposes the underlying emulator field.
     *
     * @return the underlying emulator field
     */
    public ScreenField raw() {
        return field;
    }

    @Override
    public String toString() {
        return "As400Field[row=" + row() + ", col=" + col()
            + ", len=" + length()
            + (isProtected() ? ", protected" : "")
            + "]";
    }
}
