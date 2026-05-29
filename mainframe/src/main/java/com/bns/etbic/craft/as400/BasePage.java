package com.bns.etbic.craft.as400;

/**
 * Base de todos los Page Object de pantallas 5250. Expone el driver compartido
 * por {@link As400Factory} (campo {@code as400}) y las consultas de pantalla que
 * usan los steps para asertar.
 */
public abstract class BasePage {

    protected final As400Driver as400 = As400Factory.getDriver();

    /** ¿La pantalla actual contiene este texto? Lee el estado vivo del host. */
    public boolean contains(String text) {
        return as400.getScreen().contains(text);
    }

    /** Texto completo de la pantalla actual (para mensajes de aserción). */
    public String text() {
        return as400.screenAsText();
    }
}
