package org.tn5250j.mainframe.elements;

public interface FieldActions {

    void typeAt(int row1Based, int col1Based, String text);

    void clearField(int row1Based, int col1Based, int length);
}
