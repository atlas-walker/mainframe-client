package com.bns.etbic.craft.as400.support;

/**
 * Configuración de la conexión al as400 para las pruebas.
 *
 * <p>host/device: -D{name} primero, luego variable de entorno, luego el default.
 * Credenciales (`user`/`password`): solo desde el entorno (obligatorias).
 */
public final class As400Config {

    private As400Config() {}

    public static String host() {
        return prop("host", "TBNSIMAGE.EBSS.BNS");
    }

    public static String deviceName() {
        return prop("deviceName", "BNS31954");
    }

    public static String user() {
        return requireEnv("user");
    }

    public static String password() {
        return requireEnv("password");
    }

    private static String prop(String name, String fallback) {
        String v = System.getProperty(name);
        if (v == null || v.isEmpty()) {
            v = System.getenv(name);
        }
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    private static String requireEnv(String name) {
        String v = System.getenv(name);
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException(
                "Missing environment variable: " + name + " (set `user` and `password`)");
        }
        return v;
    }
}
