package com.bns.etbic.craft.as400.support;

import io.cucumber.java.After;

/**
 * Cierra la sesión de AS/400 al final de cada escenario, haya conectado o no
 * ({@link As400Session#close()} es no-op si nunca se abrió). Mantiene el driver
 * efímero: nunca vive más que el escenario.
 */
public final class Hooks {

    private final As400Session session;

    public Hooks(As400Session session) {
        this.session = session;
    }

    @After
    public void closeAs400Session() {
        session.close();
    }
}
