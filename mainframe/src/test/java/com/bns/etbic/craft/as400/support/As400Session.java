package com.bns.etbic.craft.as400.support;

import java.time.Duration;

import com.bns.etbic.craft.as400.As400Driver;

/**
 * Sesión de as400 con alcance de UN escenario.
 *
 * <p>El driver es EFÍMERO a propósito: se conecta de forma perezosa la primera vez
 * que un step pide {@link #driver()}, y {@link Hooks} lo cierra al terminar el
 * escenario. Así el mismo escenario puede:
 * <ul>
 *   <li>ser 100% as400: abre la sesión en su primer step y la reúsa;</li>
 *   <li>ser mayormente Playwright y tocar el as400 en un solo step: conecta
 *       solo en ese step;</li>
 *   <li>no tocar el as400: no abre ninguna conexión.</li>
 * </ul>
 *
 * <p>PicoContainer (cucumber-picocontainer) crea una instancia por escenario y la
 * comparte entre las clases de steps y los hooks. Convive sin problema con una
 * sesión de Playwright análoga en el mismo escenario.
 */
public final class As400Session implements AutoCloseable {

    private As400Driver driver;

    /** Conecta la primera vez que se llama; reutiliza la conexión después. */
    public As400Driver driver() {
        if (driver == null) {
            As400Driver d = As400Driver.builder()
                .host(As400Config.host()).port(23)
                .codePage("37")
                .deviceName(As400Config.deviceName())
                .headless(true)
                .defaultTimeout(Duration.ofSeconds(30))
                .build();
            d.connect();
            driver = d;
        }
        return driver;
    }

    public boolean isOpen() {
        return driver != null;
    }

    @Override
    public void close() {
        if (driver != null) {
            try {
                driver.disconnect();
            } finally {
                driver = null;
            }
        }
    }
}
