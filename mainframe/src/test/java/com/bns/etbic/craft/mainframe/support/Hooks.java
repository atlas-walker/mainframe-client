package com.bns.etbic.craft.mainframe.support;

import io.cucumber.java.After;

/**
 * Cierra la sesión de mainframe al final de cada escenario, haya conectado o no
 * ({@link MainframeSession#close()} es no-op si nunca se abrió). Mantiene el driver
 * efímero: nunca vive más que el escenario.
 */
public final class Hooks {

    private final MainframeSession session;

    public Hooks(MainframeSession session) {
        this.session = session;
    }

    @After
    public void closeMainframeSession() {
        session.close();
    }
}
