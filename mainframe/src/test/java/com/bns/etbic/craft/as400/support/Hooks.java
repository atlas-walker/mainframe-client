package com.bns.etbic.craft.as400.support;

import com.bns.etbic.craft.as400.As400Factory;

import io.cucumber.java8.En;

/**
 * Cucumber lifecycle hooks (Java 8 lambda style).
 *
 * <p>Closes the AS/400 driver after every scenario, whether or not it connected
 * ({@link As400Factory#close()} is a no-op when no session was opened). This keeps
 * the shared driver ephemeral: it never outlives a single scenario.
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public class Hooks implements En {

    public Hooks() {
        After(() -> As400Factory.close());
    }
}
