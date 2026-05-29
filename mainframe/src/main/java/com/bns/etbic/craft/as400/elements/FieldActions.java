package com.bns.etbic.craft.as400.elements;

/**
 * Low-level operations a {@link As400Field} performs on the live screen. The driver
 * supplies the implementation, keeping fields decoupled from the emulator API.
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public interface FieldActions {

    /**
     * Types text starting at the given screen position.
     *
     * @param row1Based the 1-based row
     * @param col1Based the 1-based column
     * @param text      the text to type
     */
    void typeAt(int row1Based, int col1Based, String text);

    /**
     * Clears the field located at the given position.
     *
     * @param row1Based the 1-based row of the field start
     * @param col1Based the 1-based column of the field start
     * @param length    the field length, in characters
     */
    void clearField(int row1Based, int col1Based, int length);
}
