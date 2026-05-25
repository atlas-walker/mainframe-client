package com.bns.etbic.craft.mainframe.locators;

import org.tn5250j.framework.tn5250.Screen5250;
import com.bns.etbic.craft.mainframe.elements.FieldActions;
import com.bns.etbic.craft.mainframe.elements.ScreenSnapshot;

public interface LocatorContext {
    Screen5250 screen();
    ScreenSnapshot snapshot();
    FieldActions fieldActions();
}
