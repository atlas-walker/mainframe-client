package com.bns.etbic.craft.as400.pages;

import com.bns.etbic.craft.as400.BasePage;
import com.bns.etbic.craft.as400.keys.Key;
import com.bns.etbic.craft.as400.locators.By;

/**
 * Page Object for the NSLC main menu, where an option is chosen by typing it into
 * the {@code Selection} field.
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public final class MainMenuPage extends BasePage {

    /**
     * Types the option into the {@code Selection} field and submits.
     *
     * <p>Uses {@code pressAndWait} because the destination screen is unknown: it
     * blocks until the host actually repaints. The outcome is then asserted against
     * the live screen via {@link #contains(String)} / {@link #text()}.
     *
     * @param option the menu option to select
     */
    public void selectOption(String option) {
        as400.findField(By.labelLeftOf("Selection")).type(option);
        as400.pressAndWait(Key.ENTER);
    }
}
