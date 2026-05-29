package com.bns.etbic.craft.as400.transport;

import java.util.Properties;

import org.tn5250j.Session5250;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.common.SessionManager;
import com.bns.etbic.craft.as400.As400Config;
import com.bns.etbic.craft.as400.As400Exception;

public final class SessionFactory {

    private SessionFactory() {}

    public static Session5250 open(As400Config opts) {
        Properties p = new Properties();
        p.put(TN5250jConstants.SESSION_HOST,        opts.host());
        p.put(TN5250jConstants.SESSION_HOST_PORT,   String.valueOf(opts.port()));
        p.put(TN5250jConstants.SESSION_CODE_PAGE,   opts.codePage());
        p.put(TN5250jConstants.SESSION_SCREEN_SIZE, opts.screenSize().token);
        p.put(TN5250jConstants.SSL_TYPE,            opts.sslType());
        if (opts.enhancedTn()) {
            p.put(TN5250jConstants.SESSION_TN_ENHANCED, "1");
        }
        p.put(TN5250jConstants.SESSION_DEVICE_NAME, opts.deviceName());

        try {
            return SessionManager.instance().openSession(p, "", opts.sessionName());
        } catch (RuntimeException e) {
            throw new As400Exception("Failed to open session to " + opts.host() + ":" + opts.port(), e);
        }
    }
}
