package com.bns.etbic.craft.as400.support;

import com.bns.etbic.craft.as400.As400Factory;

import io.cucumber.java.After;

/**
 * Cierra el driver de AS/400 al final de cada escenario, haya conectado o no
 * ({@link As400Factory#close()} es no-op si nunca se abrió). Mantiene el driver
 * efímero: nunca vive más que el escenario.
 */
public final class Hooks {

    @After
    public void closeAs400Session() {
        As400Factory.close();
    }
}
