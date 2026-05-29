package com.bns.etbic.craft.as400.locators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenFields;
import com.bns.etbic.craft.as400.As400Exception;
import com.bns.etbic.craft.as400.elements.As400Field;
import com.bns.etbic.craft.as400.elements.ScreenRegion;
import com.bns.etbic.craft.as400.elements.ScreenSnapshot;

/**
 * Factory of {@link Locator}s for finding input fields and text regions on a 5250
 * screen, by position, by nearby label, by index, or by text/pattern.
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public final class By {

    private By() {}

    /**
     * Locates the input field at an exact position.
     *
     * @param row the 1-based row
     * @param col the 1-based column
     * @return a locator for the field at that position
     */
    public static Locator<As400Field> at(int row, int col) {
        return new AtPositionLocator(row, col);
    }

    /**
     * Locates a fixed-length text region at an exact position.
     *
     * @param row    the 1-based row
     * @param col    the 1-based starting column
     * @param length the region length, in characters
     * @return a locator for the text region
     */
    public static Locator<ScreenRegion> textAt(int row, int col, int length) {
        return new TextAtLocator(row, col, length);
    }

    /**
     * Locates the first input field to the right of a label on the same row.
     *
     * @param labelText the label text to search for
     * @return a locator for the field following the label
     */
    public static Locator<As400Field> labelLeftOf(String labelText) {
        return new LabelLeftOfLocator(labelText);
    }

    /**
     * Locates the first input field below a label, in the label's column.
     *
     * @param labelText the label text to search for
     * @return a locator for the field beneath the label
     */
    public static Locator<As400Field> labelAbove(String labelText) {
        return new LabelAboveLocator(labelText);
    }

    /**
     * Locates an input field by its 1-based order on the screen.
     *
     * @param oneBasedIndex the 1-based field index
     * @return a locator for that field
     */
    public static Locator<As400Field> fieldIndex(int oneBasedIndex) {
        return new FieldIndexLocator(oneBasedIndex);
    }

    /**
     * Locates a region containing the given literal text.
     *
     * @param needle the literal text to find (matched verbatim)
     * @return a locator for the matching region
     */
    public static Locator<ScreenRegion> containingText(String needle) {
        return new ContainingTextLocator(Pattern.quote(needle));
    }

    /**
     * Locates a region matching the given regular expression.
     *
     * @param regex the regular expression to match
     * @return a locator for the matching region
     */
    public static Locator<ScreenRegion> matching(String regex) {
        return new ContainingTextLocator(regex);
    }

    /**
     * Locates the first input field on the screen.
     *
     * @return a locator for the first input field
     */
    public static Locator<As400Field> firstInputField() {
        return new FirstInputFieldLocator();
    }

    // ----- implementations -----

    private static final class AtPositionLocator implements Locator<As400Field> {
        final int row, col;
        AtPositionLocator(int row, int col) { this.row = row; this.col = col; }
        @Override public As400Field locate(LocatorContext ctx) {
            Screen5250 s = ctx.screen();
            ScreenFields fields = s.getScreenFields();
            if (fields == null) {
                throw new As400Exception("No fields on screen (host has not drawn input fields yet)");
            }
            ScreenField f = fields.findByPosition(row - 1, col - 1);
            if (f == null) {
                throw new As400Exception("No input field at row=" + row + ", col=" + col);
            }
            return new As400Field(f, ctx.fieldActions());
        }
        @Override public String describe() { return "By.at(" + row + "," + col + ")"; }
    }

    private static final class TextAtLocator implements Locator<ScreenRegion> {
        final int row, col, length;
        TextAtLocator(int row, int col, int length) {
            this.row = row; this.col = col; this.length = length;
        }
        @Override public ScreenRegion locate(LocatorContext ctx) {
            ScreenSnapshot snap = ctx.snapshot();
            return new ScreenRegion(row, col, length, snap.textAt(row, col, length));
        }
        @Override public String describe() {
            return "By.textAt(" + row + "," + col + "," + length + ")";
        }
    }

    private static final class LabelLeftOfLocator implements Locator<As400Field> {
        final String label;
        LabelLeftOfLocator(String label) { this.label = label; }
        @Override public As400Field locate(LocatorContext ctx) {
            ScreenSnapshot snap = ctx.snapshot();
            Screen5250 s = ctx.screen();
            ScreenFields fields = s.getScreenFields();
            if (fields == null) {
                throw new As400Exception("No fields on screen");
            }
            for (int row = 1; row <= snap.rows(); row++) {
                String rowText = snap.row(row);
                int idx = rowText.indexOf(label);
                if (idx < 0) continue;
                int searchCol = idx + label.length() + 1;
                for (int c = searchCol; c <= snap.cols(); c++) {
                    ScreenField f = fields.findByPosition(row - 1, c - 1);
                    if (f != null && !f.isBypassField()) {
                        return new As400Field(f, ctx.fieldActions());
                    }
                }
            }
            throw new As400Exception("No input field found to the right of label \"" + label + "\"");
        }
        @Override public String describe() { return "By.labelLeftOf(\"" + label + "\")"; }
    }

    private static final class LabelAboveLocator implements Locator<As400Field> {
        final String label;
        LabelAboveLocator(String label) { this.label = label; }
        @Override public As400Field locate(LocatorContext ctx) {
            ScreenSnapshot snap = ctx.snapshot();
            ScreenFields fields = ctx.screen().getScreenFields();
            if (fields == null) {
                throw new As400Exception("No fields on screen");
            }
            for (int row = 1; row < snap.rows(); row++) {
                String rowText = snap.row(row);
                int idx = rowText.indexOf(label);
                if (idx < 0) continue;
                int labelCol = idx + 1;
                for (int rBelow = row + 1; rBelow <= snap.rows(); rBelow++) {
                    ScreenField f = fields.findByPosition(rBelow - 1, labelCol - 1);
                    if (f != null && !f.isBypassField()) {
                        return new As400Field(f, ctx.fieldActions());
                    }
                }
            }
            throw new As400Exception("No input field found below label \"" + label + "\"");
        }
        @Override public String describe() { return "By.labelAbove(\"" + label + "\")"; }
    }

    private static final class FieldIndexLocator implements Locator<As400Field> {
        final int index;
        FieldIndexLocator(int index) { this.index = index; }
        @Override public As400Field locate(LocatorContext ctx) {
            ScreenFields fields = ctx.screen().getScreenFields();
            if (fields == null || index < 1 || index > fields.getFieldCount()) {
                throw new As400Exception("Field index out of range: " + index);
            }
            return new As400Field(fields.getField(index - 1), ctx.fieldActions());
        }
        @Override public String describe() { return "By.fieldIndex(" + index + ")"; }
    }

    private static final class FirstInputFieldLocator implements Locator<As400Field> {
        @Override public As400Field locate(LocatorContext ctx) {
            ScreenFields fields = ctx.screen().getScreenFields();
            if (fields == null) {
                throw new As400Exception("No fields on screen");
            }
            ScreenField f = fields.getFirstInputField();
            if (f == null) {
                throw new As400Exception("No input fields on screen");
            }
            return new As400Field(f, ctx.fieldActions());
        }
        @Override public String describe() { return "By.firstInputField()"; }
    }

    private static final class ContainingTextLocator implements Locator<ScreenRegion> {
        final String regex;
        ContainingTextLocator(String regex) { this.regex = regex; }
        @Override public ScreenRegion locate(LocatorContext ctx) {
            ScreenSnapshot snap = ctx.snapshot();
            Pattern p = Pattern.compile(regex);
            for (int row = 1; row <= snap.rows(); row++) {
                String text = snap.row(row);
                Matcher m = p.matcher(text);
                if (m.find()) {
                    return new ScreenRegion(row, m.start() + 1, m.end() - m.start(), m.group());
                }
            }
            throw new As400Exception("No text matching /" + regex + "/ on screen");
        }
        @Override public List<ScreenRegion> locateAll(LocatorContext ctx) {
            ScreenSnapshot snap = ctx.snapshot();
            Pattern p = Pattern.compile(regex);
            List<ScreenRegion> out = new ArrayList<>();
            for (int row = 1; row <= snap.rows(); row++) {
                Matcher m = p.matcher(snap.row(row));
                while (m.find()) {
                    out.add(new ScreenRegion(row, m.start() + 1, m.end() - m.start(), m.group()));
                }
            }
            return out;
        }
        @Override public String describe() { return "By.matching(/" + regex + "/)"; }
    }
}
