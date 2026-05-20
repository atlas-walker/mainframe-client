package org.tn5250j.mainframe.locators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenFields;
import org.tn5250j.mainframe.MainframeException;
import org.tn5250j.mainframe.elements.MainframeField;
import org.tn5250j.mainframe.elements.ScreenRegion;
import org.tn5250j.mainframe.elements.ScreenSnapshot;

public final class By {

    private By() {}

    public static Locator<MainframeField> at(int row, int col) {
        return new AtPositionLocator(row, col);
    }

    public static Locator<ScreenRegion> textAt(int row, int col, int length) {
        return new TextAtLocator(row, col, length);
    }

    public static Locator<MainframeField> labelLeftOf(String labelText) {
        return new LabelLeftOfLocator(labelText);
    }

    public static Locator<MainframeField> labelAbove(String labelText) {
        return new LabelAboveLocator(labelText);
    }

    public static Locator<MainframeField> fieldIndex(int oneBasedIndex) {
        return new FieldIndexLocator(oneBasedIndex);
    }

    public static Locator<ScreenRegion> containingText(String needle) {
        return new ContainingTextLocator(Pattern.quote(needle));
    }

    public static Locator<ScreenRegion> matching(String regex) {
        return new ContainingTextLocator(regex);
    }

    public static Locator<MainframeField> firstInputField() {
        return new FirstInputFieldLocator();
    }

    // ----- implementations -----

    private static final class AtPositionLocator implements Locator<MainframeField> {
        final int row, col;
        AtPositionLocator(int row, int col) { this.row = row; this.col = col; }
        @Override public MainframeField locate(LocatorContext ctx) {
            Screen5250 s = ctx.screen();
            ScreenFields fields = s.getScreenFields();
            if (fields == null) {
                throw new MainframeException("No fields on screen (host has not drawn input fields yet)");
            }
            ScreenField f = fields.findByPosition(row - 1, col - 1);
            if (f == null) {
                throw new MainframeException("No input field at row=" + row + ", col=" + col);
            }
            return new MainframeField(f, ctx.fieldActions());
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

    private static final class LabelLeftOfLocator implements Locator<MainframeField> {
        final String label;
        LabelLeftOfLocator(String label) { this.label = label; }
        @Override public MainframeField locate(LocatorContext ctx) {
            ScreenSnapshot snap = ctx.snapshot();
            Screen5250 s = ctx.screen();
            ScreenFields fields = s.getScreenFields();
            if (fields == null) {
                throw new MainframeException("No fields on screen");
            }
            for (int row = 1; row <= snap.rows(); row++) {
                String rowText = snap.row(row);
                int idx = rowText.indexOf(label);
                if (idx < 0) continue;
                int searchCol = idx + label.length() + 1;
                for (int c = searchCol; c <= snap.cols(); c++) {
                    ScreenField f = fields.findByPosition(row - 1, c - 1);
                    if (f != null && !f.isBypassField()) {
                        return new MainframeField(f, ctx.fieldActions());
                    }
                }
            }
            throw new MainframeException("No input field found to the right of label \"" + label + "\"");
        }
        @Override public String describe() { return "By.labelLeftOf(\"" + label + "\")"; }
    }

    private static final class LabelAboveLocator implements Locator<MainframeField> {
        final String label;
        LabelAboveLocator(String label) { this.label = label; }
        @Override public MainframeField locate(LocatorContext ctx) {
            ScreenSnapshot snap = ctx.snapshot();
            ScreenFields fields = ctx.screen().getScreenFields();
            if (fields == null) {
                throw new MainframeException("No fields on screen");
            }
            for (int row = 1; row < snap.rows(); row++) {
                String rowText = snap.row(row);
                int idx = rowText.indexOf(label);
                if (idx < 0) continue;
                int labelCol = idx + 1;
                for (int rBelow = row + 1; rBelow <= snap.rows(); rBelow++) {
                    ScreenField f = fields.findByPosition(rBelow - 1, labelCol - 1);
                    if (f != null && !f.isBypassField()) {
                        return new MainframeField(f, ctx.fieldActions());
                    }
                }
            }
            throw new MainframeException("No input field found below label \"" + label + "\"");
        }
        @Override public String describe() { return "By.labelAbove(\"" + label + "\")"; }
    }

    private static final class FieldIndexLocator implements Locator<MainframeField> {
        final int index;
        FieldIndexLocator(int index) { this.index = index; }
        @Override public MainframeField locate(LocatorContext ctx) {
            ScreenFields fields = ctx.screen().getScreenFields();
            if (fields == null || index < 1 || index > fields.getFieldCount()) {
                throw new MainframeException("Field index out of range: " + index);
            }
            return new MainframeField(fields.getField(index - 1), ctx.fieldActions());
        }
        @Override public String describe() { return "By.fieldIndex(" + index + ")"; }
    }

    private static final class FirstInputFieldLocator implements Locator<MainframeField> {
        @Override public MainframeField locate(LocatorContext ctx) {
            ScreenFields fields = ctx.screen().getScreenFields();
            if (fields == null) {
                throw new MainframeException("No fields on screen");
            }
            ScreenField f = fields.getFirstInputField();
            if (f == null) {
                throw new MainframeException("No input fields on screen");
            }
            return new MainframeField(f, ctx.fieldActions());
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
            throw new MainframeException("No text matching /" + regex + "/ on screen");
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
