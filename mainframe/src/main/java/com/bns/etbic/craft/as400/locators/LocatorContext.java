package com.bns.etbic.craft.as400.locators;

import org.tn5250j.framework.tn5250.Screen5250;
import com.bns.etbic.craft.as400.elements.FieldActions;
import com.bns.etbic.craft.as400.elements.ScreenSnapshot;

public interface LocatorContext {
    Screen5250 screen();
    ScreenSnapshot snapshot();
    FieldActions fieldActions();
}
