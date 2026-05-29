package com.bns.etbic.craft.as400.locators;

import org.tn5250j.framework.tn5250.Screen5250;
import com.bns.etbic.craft.as400.elements.FieldActions;
import com.bns.etbic.craft.as400.elements.ScreenSnapshot;

/**
 * The context a {@link Locator} searches: the live emulator screen, an immutable
 * snapshot of it, and the actions a located field can perform. Supplied by the driver.
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public interface LocatorContext {

    /**
     * Returns the live emulator screen.
     *
     * @return the live emulator screen
     */
    Screen5250 screen();

    /**
     * Returns an immutable snapshot of the screen taken at search time.
     *
     * @return the screen snapshot
     */
    ScreenSnapshot snapshot();

    /**
     * Returns the actions a located field uses to interact with the screen.
     *
     * @return the field actions
     */
    FieldActions fieldActions();
}
