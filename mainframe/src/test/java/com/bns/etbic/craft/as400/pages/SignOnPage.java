package com.bns.etbic.craft.as400.pages;

import com.bns.etbic.craft.as400.BasePage;
import com.bns.etbic.craft.as400.keys.Key;
import com.bns.etbic.craft.as400.locators.By;
import com.bns.etbic.craft.as400.waits.As400Conditions;

/** Pantalla de sign-on (User / Password). */
public final class SignOnPage extends BasePage {

    /** Espera el sign-on, escribe credenciales, envía y devuelve el menú ya cargado. */
    public MainMenuPage signOn(String user, String password) {
        as400.waitFor(As400Conditions.inputReady());
        as400.findField(By.labelLeftOf("User")).type(user);
        as400.findField(By.labelLeftOf("Password")).type(password);
        as400.press(Key.ENTER);
        // "Selection" no existe en el sign-on: su aparición marca que llegamos
        // al menú (sin espera ciega ni timeouts falsos).
        as400.waitFor(As400Conditions.textPresent("Selection"));
        return new MainMenuPage();
    }
}
