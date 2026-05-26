package com.bns.etbic.craft.as400.elements;

public interface FieldActions {

    void typeAt(int row1Based, int col1Based, String text);

    void clearField(int row1Based, int col1Based, int length);
}
