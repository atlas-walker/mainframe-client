package com.bns.etbic.craft.as400.waits;

import java.time.Duration;
import java.util.Arrays;

import com.bns.etbic.craft.as400.elements.ScreenSnapshot;

/**
 * Factory of the {@link ExpectedCondition}s used with the driver's {@code waitFor}
 * methods to wait for specific host states (input ready, text present, cursor
 * position, screen stability).
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public final class As400Conditions {

    private As400Conditions() {}

    /**
     * Waits until the host is ready to accept keystrokes.
     *
     * @return a condition met when the keyboard is unlocked and ready for input
     */
    public static ExpectedCondition<Boolean> inputReady() {
        return new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(ScreenSnapshot snap) {
                return snap.inputReady() ? Boolean.TRUE : null;
            }
            @Override public String describe() { return "inputReady()"; }
        };
    }

    /**
     * Waits until a piece of text appears anywhere on the screen.
     *
     * @param needle the text to look for anywhere on the screen
     * @return a condition met when the screen contains {@code needle}
     */
    public static ExpectedCondition<Boolean> textPresent(final String needle) {
        return new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(ScreenSnapshot snap) {
                return snap.contains(needle) ? Boolean.TRUE : null;
            }
            @Override public String describe() { return "textPresent(\"" + needle + "\")"; }
        };
    }

    /**
     * Waits until a piece of text appears on a specific row.
     *
     * @param row    the 1-based row to inspect
     * @param needle the text to look for on that row
     * @return a condition met when the given row contains {@code needle}
     */
    public static ExpectedCondition<Boolean> textPresent(final int row, final String needle) {
        return new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(ScreenSnapshot snap) {
                return snap.row(row).contains(needle) ? Boolean.TRUE : null;
            }
            @Override public String describe() {
                return "textPresent(row=" + row + ", \"" + needle + "\")";
            }
        };
    }

    /**
     * Waits until an exact text appears at a specific position.
     *
     * @param row      the 1-based row
     * @param col      the 1-based column
     * @param expected the exact text expected starting at that position
     * @return a condition met when the text at the position equals {@code expected}
     */
    public static ExpectedCondition<Boolean> textAt(final int row, final int col, final String expected) {
        return new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(ScreenSnapshot snap) {
                String actual = snap.textAt(row, col, expected.length());
                return actual.equals(expected) ? Boolean.TRUE : null;
            }
            @Override public String describe() {
                return "textAt(" + row + "," + col + ", \"" + expected + "\")";
            }
        };
    }

    /**
     * Waits until the cursor reaches a specific position.
     *
     * @param row the 1-based row
     * @param col the 1-based column
     * @return a condition met when the cursor sits at the given position
     */
    public static ExpectedCondition<Boolean> cursorAt(final int row, final int col) {
        return new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(ScreenSnapshot snap) {
                return (snap.cursorRow() == row && snap.cursorCol() == col) ? Boolean.TRUE : null;
            }
            @Override public String describe() {
                return "cursorAt(" + row + "," + col + ")";
            }
        };
    }

    /**
     * Waits until the screen content stops changing for a quiet window. Useful when
     * the destination text is unknown but the screen should settle.
     *
     * @param window the quiet period the screen must remain unchanged for
     * @return a condition met once the screen has been stable for {@code window}
     */
    public static ExpectedCondition<Boolean> screenStable(final Duration window) {
        final long quietNanos = window.toNanos();
        return new ExpectedCondition<Boolean>() {
            // Compara el CONTENIDO de la pantalla entre polls. `timestamp` no sirve
            // porque es el instante en que se creó el snapshot (cambia en cada poll),
            // no el de la última vez que el host repintó la pantalla.
            char[] lastText = null;
            long stableSince = -1;
            @Override public Boolean apply(ScreenSnapshot snap) {
                long now = System.nanoTime();
                char[] text = snap.textPlane();
                if (lastText == null || !Arrays.equals(text, lastText)) {
                    lastText = text;       // primer muestreo o la pantalla cambió
                    stableSince = now;     // reinicia la ventana de quietud
                    return null;
                }
                return (now - stableSince) >= quietNanos ? Boolean.TRUE : null;
            }
            @Override public String describe() {
                return "screenStable(" + window.toMillis() + "ms)";
            }
        };
    }
}
