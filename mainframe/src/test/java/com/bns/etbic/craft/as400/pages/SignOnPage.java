package com.bns.etbic.craft.as400.pages;

import com.bns.etbic.craft.as400.BasePage;
import com.bns.etbic.craft.as400.keys.Key;
import com.bns.etbic.craft.as400.locators.By;
import com.bns.etbic.craft.as400.waits.As400Conditions;

/**
 * Page Object for the AS/400 Sign-On screen (User / Password).
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public final class SignOnPage extends BasePage {

    /**
     * Waits for the Sign-On screen to be ready, types the credentials, submits and
     * waits for the main menu to be painted.
     *
     * <p>Arrival at the menu is detected by the appearance of the {@code "Selection"}
     * field (which does not exist on the Sign-On screen), so no blind sleeps are used.
     *
     * @param user     the user profile to sign on with
     * @param password the password for {@code user}
     * @return the {@link MainMenuPage}, already loaded
     */
    public MainMenuPage signOn(String user, String password) {
        as400.waitFor(As400Conditions.inputReady());
        as400.findField(By.labelLeftOf("User")).type(user);
        as400.findField(By.labelLeftOf("Password")).type(password);
        as400.press(Key.ENTER);
        as400.waitFor(As400Conditions.textPresent("Selection"));
        return new MainMenuPage();
    }
}
