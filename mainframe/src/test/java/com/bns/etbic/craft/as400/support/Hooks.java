package com.bns.etbic.craft.as400.support;

import com.bns.etbic.craft.as400.As400Factory;

import io.cucumber.java8.En;

/**
 * Cierra el driver de AS/400 al final de cada escenario, haya conectado o no
 * ({@link As400Factory#close()} es no-op si nunca se abrió). Mantiene el driver
 * efímero: nunca vive más que el escenario.
 */
public class Hooks implements En {

    public Hooks() {
        After(() -> As400Factory.close());
    }
}
