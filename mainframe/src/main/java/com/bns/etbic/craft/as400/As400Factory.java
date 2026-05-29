package com.bns.etbic.craft.as400;

import java.time.Duration;

import org.tn5250j.TN5250jConstants;

/**
 * Punto único de creación y cierre de lo relacionado al AS/400. Mantiene un
 * driver compartido (se crea y conecta la primera vez que se pide) y sabe leer
 * la configuración desde propiedades {@code -Das400.*}.
 */
public final class As400Factory {

    private static As400Config config;
    private static As400Driver driver;

    private As400Factory() {}

    /**
     * Arma un {@link As400Config} nuevo en cada llamada leyendo {@code -Das400.*}
     * (con valores por defecto).
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

    /** Driver compartido: lo crea y conecta la primera vez. */
    public static synchronized As400Driver getDriver() {
        if (driver == null) {
            config = config();
            driver = new As400Driver(config);
            driver.connect();
        }
        return driver;
    }

    /** La config con la que se creó el driver actual (o una recién leída si no hay driver). */
    public static synchronized As400Config currentConfig() {
        return config != null ? config : config();
    }

    /** Cierra el driver y limpia el estado compartido. Seguro de llamar sin driver abierto. */
    public static synchronized void close() {
        if (driver != null) {
            driver.disconnect();
            driver = null;
        }
        config = null;
    }

    private static String prop(String name, String def) {
        return System.getProperty("as400." + name, def);
    }
}
