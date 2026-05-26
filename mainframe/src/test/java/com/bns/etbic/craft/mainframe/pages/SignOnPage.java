package com.bns.etbic.craft.mainframe.pages;

import com.bns.etbic.craft.mainframe.MainframeDriver;
import com.bns.etbic.craft.mainframe.keys.Key;
import com.bns.etbic.craft.mainframe.locators.By;
import com.bns.etbic.craft.mainframe.waits.MainframeConditions;

/** Pantalla de sign-on (User / Password). */
public final class SignOnPage {

    private final MainframeDriver driver;

    public SignOnPage(MainframeDriver driver) {
        this.driver = driver;
    }

    public SignOnPage waitUntilReady() {
        driver.waitFor(MainframeConditions.inputReady());
        return this;
    }

    /** Escribe credenciales, envía y espera el menú. */
    public MainMenuPage signOn(String user, String password) {
        driver.findField(By.labelLeftOf("User")).type(user);
        driver.findField(By.labelLeftOf("Password")).type(password);
        driver.press(Key.ENTER);
        // "Selection" no existe en el sign-on: su aparición marca que llegamos
        // al menú (sin espera ciega ni timeouts falsos).
        driver.waitFor(MainframeConditions.textPresent("Selection"));
        return new MainMenuPage(driver);
    }
}
