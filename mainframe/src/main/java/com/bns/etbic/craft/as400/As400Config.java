package com.bns.etbic.craft.as400;

import java.time.Duration;

import org.tn5250j.TN5250jConstants;

/**
 * Configuración inmutable de una conexión al AS/400. Normalmente se obtiene con
 * {@link As400Factory#config()}, que la llena desde propiedades {@code -Das400.*}.
 */
public record As400Config(
        String host,
        int port,
        String codePage,
        String deviceName,
        String sslType,
        ScreenSize screenSize,
        boolean enhancedTn,
        boolean headless,
        Duration defaultTimeout,
        String sessionName,
        String user,
        String password) {

    public enum ScreenSize {
        S24x80(TN5250jConstants.SCREEN_SIZE_24X80_STR),
        S27x132(TN5250jConstants.SCREEN_SIZE_27X132_STR);

        public final String token;

        ScreenSize(String token) {
            this.token = token;
        }
    }

    public As400Config {
        if (host == null || host.isBlank()) {
            throw new As400Exception("host is required");
        }
        if (port <= 0 || port > 65535) {
            throw new As400Exception("port out of range: " + port);
        }
        if (deviceName == null || deviceName.isBlank()) {
            throw new As400Exception(
                "deviceName (WorkstationID) is required — the AS/400 disconnects without it");
        }
    }
}
