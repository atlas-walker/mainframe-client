package org.tn5250j.mainframe.locators;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.mainframe.elements.FieldActions;
import org.tn5250j.mainframe.elements.ScreenSnapshot;

public interface LocatorContext {
    Screen5250 screen();
    ScreenSnapshot snapshot();
    FieldActions fieldActions();
}
