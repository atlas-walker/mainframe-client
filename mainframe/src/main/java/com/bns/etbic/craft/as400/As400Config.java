package com.bns.etbic.craft.as400;

import java.time.Duration;

import org.tn5250j.TN5250jConstants;

/**
 * Immutable configuration for a single AS/400 (IBM i) 5250 connection.
 *
 * <p>Instances are normally obtained from {@link As400Factory#config()}, which
 * populates every component from {@code -Das400.*} system properties (falling
 * back to sensible defaults). The record can also be constructed directly when a
 * caller needs full control over the connection parameters.
 *
 * <p>The compact constructor enforces the invariants that the host requires to
 * accept a session: a non-blank {@code host}, a valid TCP {@code port}, and a
 * non-blank {@code deviceName} (the AS/400 drops the connection without a
 * workstation id).
 *
 * @param host           host name or IP of the AS/400 to connect to
 * @param port           TCP port of the telnet/5250 service (typically {@code 23})
 * @param codePage       EBCDIC code page used to encode the session (e.g. {@code "37"})
 * @param deviceName     workstation id reported to the host; mandatory
 * @param sslType        SSL handshake type, as defined by {@link TN5250jConstants}
 * @param screenSize     terminal geometry to negotiate with the host
 * @param enhancedTn     whether to request the enhanced TN5250 protocol options
 * @param headless       {@code true} to run without opening a terminal window
 * @param defaultTimeout default wait applied by the driver's blocking operations
 * @param sessionName    logical name of the session, used for logging and the window title
 * @param user           user profile used to sign on (may be empty if signed on elsewhere)
 * @param password       password for {@code user} (may be empty if signed on elsewhere)
 *
 * @author Andres Acosta
 * @since 1.0.14
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

    /**
     * Terminal geometries supported by the 5250 data stream. Each constant carries
     * the {@link TN5250jConstants} token expected by the underlying emulator.
     *
     * @author Andres Acosta
     * @since 1.0.14
     */
    public enum ScreenSize {

        /** 24 rows by 80 columns (the default 5250 screen). */
        S24x80(TN5250jConstants.SCREEN_SIZE_24X80_STR),

        /** 27 rows by 132 columns (wide screen). */
        S27x132(TN5250jConstants.SCREEN_SIZE_27X132_STR);

        /** Token passed verbatim to the emulator's session properties. */
        public final String token;

        ScreenSize(String token) {
            this.token = token;
        }
    }

    /**
     * Validates the connection invariants required by the host.
     *
     * @throws As400Exception if {@code host} or {@code deviceName} is blank, or if
     *                        {@code port} is outside the {@code 1..65535} range
     */
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
