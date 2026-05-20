package org.tn5250j.mainframe.waits;

import java.time.Duration;

import org.tn5250j.mainframe.elements.ScreenSnapshot;

public final class MainframeConditions {

    private MainframeConditions() {}

    public static ExpectedCondition<Boolean> inputReady() {
        return new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(ScreenSnapshot snap) {
                return snap.inputReady() ? Boolean.TRUE : null;
            }
            @Override public String describe() { return "inputReady()"; }
        };
    }

    public static ExpectedCondition<Boolean> textPresent(final String needle) {
        return new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(ScreenSnapshot snap) {
                return snap.contains(needle) ? Boolean.TRUE : null;
            }
            @Override public String describe() { return "textPresent(\"" + needle + "\")"; }
        };
    }

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

    public static ExpectedCondition<Boolean> screenStable(final Duration window) {
        final long quietNanos = window.toNanos();
        return new ExpectedCondition<Boolean>() {
            long firstSeenAt = -1;
            long lastTimestamp = -1;
            @Override public Boolean apply(ScreenSnapshot snap) {
                long now = System.nanoTime();
                if (snap.timestamp() != lastTimestamp) {
                    lastTimestamp = snap.timestamp();
                    firstSeenAt = now;
                    return null;
                }
                if (firstSeenAt < 0) {
                    firstSeenAt = now;
                    return null;
                }
                return (now - firstSeenAt) >= quietNanos ? Boolean.TRUE : null;
            }
            @Override public String describe() {
                return "screenStable(" + window.toMillis() + "ms)";
            }
        };
    }
}
