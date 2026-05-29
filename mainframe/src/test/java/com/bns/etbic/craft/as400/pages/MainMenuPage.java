package com.bns.etbic.craft.as400.pages;

import com.bns.etbic.craft.as400.BasePage;
import com.bns.etbic.craft.as400.keys.Key;
import com.bns.etbic.craft.as400.locators.By;

/** Menú NSLC: se elige una opción escribiéndola en el campo Selection. */
public final class MainMenuPage extends BasePage {

    /**
     * Escribe la opción en el campo Selection y envía. Usa pressAndWait porque no
     * sabemos qué pantalla traerá la opción: espera a que el host repinte de verdad.
     */
    public As400Screen selectOption(String option) {
        as400.findField(By.labelLeftOf("Selection")).type(option);
        return new As400Screen(as400.pressAndWait(Key.ENTER));
    }
}
