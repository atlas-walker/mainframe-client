package com.bns.etbic.craft.as400.pages;

import com.bns.etbic.craft.as400.As400Driver;
import com.bns.etbic.craft.as400.keys.Key;
import com.bns.etbic.craft.as400.locators.By;
import com.bns.etbic.craft.as400.waits.As400Conditions;

/** Pantalla de sign-on (User / Password). */
public final class SignOnPage {

    private final As400Driver driver;

    public SignOnPage(As400Driver driver) {
        this.driver = driver;
    }

    public SignOnPage waitUntilReady() {
        driver.waitFor(As400Conditions.inputReady());
        return this;
    }

    /** Escribe credenciales, envía y espera el menú. */
    public MainMenuPage signOn(String user, String password) {
        driver.findField(By.labelLeftOf("User")).type(user);
        driver.findField(By.labelLeftOf("Password")).type(password);
        driver.press(Key.ENTER);
        // "Selection" no existe en el sign-on: su aparición marca que llegamos
        // al menú (sin espera ciega ni timeouts falsos).
        driver.waitFor(As400Conditions.textPresent("Selection"));
        return new MainMenuPage(driver);
    }
}
