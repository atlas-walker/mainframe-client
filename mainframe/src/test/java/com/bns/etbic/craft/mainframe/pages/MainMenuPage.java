package com.bns.etbic.craft.mainframe.pages;

import com.bns.etbic.craft.mainframe.MainframeDriver;
import com.bns.etbic.craft.mainframe.keys.Key;
import com.bns.etbic.craft.mainframe.locators.By;

/** Menú NSLC: se elige una opción escribiéndola en el campo Selection. */
public final class MainMenuPage {

    private final MainframeDriver driver;

    public MainMenuPage(MainframeDriver driver) {
        this.driver = driver;
    }

    /**
     * Escribe la opción en el campo Selection y envía. Usa pressAndWait porque no
     * sabemos qué pantalla traerá la opción: espera a que el host repinte de verdad.
     */
    public MainframeScreen selectOption(String option) {
        driver.findField(By.labelLeftOf("Selection")).type(option);
        return new MainframeScreen(driver.pressAndWait(Key.ENTER));
    }
}
