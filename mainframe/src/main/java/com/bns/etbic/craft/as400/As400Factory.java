package com.bns.etbic.craft.as400;

import java.time.Duration;

import org.tn5250j.TN5250jConstants;

/**
 * Single entry point for creating, sharing and tearing down everything related to
 * an AS/400 (IBM i) session.
 *
 * <p>The factory keeps one shared {@link As400Driver} that is created and connected
 * lazily on the first call to {@link #getDriver()} and reused afterwards. This lets
 * Page Objects (see {@link BasePage}) obtain the live driver without threading it
 * through constructors, and lets test glue close it with a single call.
 *
 * <p>Configuration is resolved from {@code -Das400.*} system properties through
 * {@link #config()}; for example {@code -Das400.host=...} and {@code -Das400.user=...}.
 *
 * <p>All mutating operations are synchronized, so the shared driver is created at
 * most once even under concurrent access.
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public final class As400Factory {

    private static As400Config config;
    private static As400Driver driver;

    private As400Factory() {}

    /**
     * Builds a fresh {@link As400Config} on every call by reading {@code -Das400.*}
     * system properties, each with a default applied when the property is absent.
     *
     * @return a new configuration reflecting the current system properties
     */
    public static As400Config config() {
        return new As400Config(
            prop("host", "TBNSIMAGE.EBSS.BNS"),
            Integer.parseInt(prop("port", "23")),
            prop("codePage", "37"),
            prop("deviceName", "BNS31954"),
            prop("sslType", TN5250jConstants.SSL_TYPE_NONE),
            As400Config.ScreenSize.valueOf(prop("screenSize", "S24x80")),
            Boolean.parseBoolean(prop("enhancedTn", "true")),
            Boolean.parseBoolean(prop("headless", "true")),
            Duration.ofSeconds(Long.parseLong(prop("timeoutSeconds", "30"))),
            prop("sessionName", "as400"),
            prop("user", ""),
            prop("password", ""));
    }

    /**
     * Returns the shared driver, creating and connecting it on first use.
     *
     * @return the connected, shared {@link As400Driver}
     */
    public static synchronized As400Driver getDriver() {
        if (driver == null) {
            config = config();
            driver = new As400Driver(config);
            driver.connect();
        }
        return driver;
    }

    /**
     * Returns the configuration the current shared driver was created with, or a
     * freshly read configuration when no driver is open yet.
     *
     * @return the active configuration, never {@code null}
     */
    public static synchronized As400Config currentConfig() {
        return config != null ? config : config();
    }

    /**
     * Disconnects the shared driver and clears the shared state. Safe to call when
     * no driver has been opened (no-op in that case).
     */
    public static synchronized void close() {
        if (driver != null) {
            driver.disconnect();
            driver = null;
        }
        config = null;
    }

    /**
     * Reads the {@code as400.<name>} system property.
     *
     * @param name the property name without the {@code as400.} prefix
     * @param def  the value returned when the property is not set
     * @return the property value, or {@code def} when absent
     */
    private static String prop(String name, String def) {
        return System.getProperty("as400." + name, def);
    }
}
