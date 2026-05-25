package com.bns.etbic.craft.mainframe;

import java.time.Duration;
import java.util.Objects;

import org.tn5250j.TN5250jConstants;

public final class MainframeOptions {

    public enum ScreenSize {
        S24x80(TN5250jConstants.SCREEN_SIZE_24X80_STR),
        S27x132(TN5250jConstants.SCREEN_SIZE_27X132_STR);

        public final String token;

        ScreenSize(String token) {
            this.token = token;
        }
    }

    private final String host;
    private final int port;
    private final String sslType;
    private final String codePage;
    private final String deviceName;
    private final ScreenSize screenSize;
    private final boolean enhancedTn;
    private final boolean headless;
    private final Duration defaultTimeout;
    private final String sessionName;

    private MainframeOptions(Builder b) {
        this.host = b.host;
        this.port = b.port;
        this.sslType = b.sslType;
        this.codePage = b.codePage;
        this.deviceName = b.deviceName;
        this.screenSize = b.screenSize;
        this.enhancedTn = b.enhancedTn;
        this.headless = b.headless;
        this.defaultTimeout = b.defaultTimeout;
        this.sessionName = b.sessionName;
    }

    public String host()              { return host; }
    public int port()                 { return port; }
    public String sslType()           { return sslType; }
    public String codePage()          { return codePage; }
    public String deviceName()        { return deviceName; }
    public ScreenSize screenSize()    { return screenSize; }
    public boolean enhancedTn()       { return enhancedTn; }
    public boolean headless()         { return headless; }
    public Duration defaultTimeout()  { return defaultTimeout; }
    public String sessionName()       { return sessionName; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String host;
        private int port = 23;
        private String sslType = TN5250jConstants.SSL_TYPE_NONE;
        private String codePage = "37";
        private String deviceName;
        private ScreenSize screenSize = ScreenSize.S24x80;
        private boolean enhancedTn = true;
        private boolean headless = true;
        private Duration defaultTimeout = Duration.ofSeconds(30);
        private String sessionName = "mainframe";

        public Builder host(String host)                  { this.host = host; return this; }
        public Builder port(int port)                     { this.port = port; return this; }
        public Builder sslType(String sslType)            { this.sslType = sslType; return this; }
        public Builder codePage(String codePage)          { this.codePage = codePage; return this; }
        public Builder deviceName(String deviceName)      { this.deviceName = deviceName; return this; }
        public Builder screenSize(ScreenSize size)        { this.screenSize = size; return this; }
        public Builder enhancedTn(boolean on)             { this.enhancedTn = on; return this; }
        public Builder headless(boolean on)               { this.headless = on; return this; }
        public Builder headed()                           { this.headless = false; return this; }
        public Builder defaultTimeout(Duration timeout)   { this.defaultTimeout = timeout; return this; }
        public Builder sessionName(String name)           { this.sessionName = name; return this; }

        public MainframeOptions build() {
            Objects.requireNonNull(host, "host is required");
            if (port <= 0 || port > 65535) {
                throw new MainframeException("port out of range: " + port);
            }
            if (deviceName == null || deviceName.trim().isEmpty()) {
                throw new MainframeException(
                    "deviceName (WorkstationID) is required — the AS/400 disconnects without it");
            }
            Objects.requireNonNull(codePage, "codePage is required");
            Objects.requireNonNull(sslType, "sslType is required");
            Objects.requireNonNull(screenSize, "screenSize is required");
            Objects.requireNonNull(defaultTimeout, "defaultTimeout is required");
            return new MainframeOptions(this);
        }
    }
}
