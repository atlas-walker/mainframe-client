package org.tn5250j.mainframe.example;

import java.nio.file.Paths;
import java.time.Duration;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import org.tn5250j.mainframe.MainframeDriver;
import org.tn5250j.mainframe.locators.By;
import org.tn5250j.mainframe.waits.MainframeConditions;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * Runnable smoke / quick-start showing the mainframe façade.
 * Equivalent to the original Smoke.java but using the new public API.
 *
 * Run with:
 *   java -cp out:lib/runtime/* org.tn5250j.mainframe.example.LoginExample
 */
public final class LoginExample {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        TN5250jLogFactory.setLogLevels(TN5250jLogger.INFO);

        String host = systemOr(args, 0, "host", "TBNSIMAGE.EBSS.BNS");
        String device = systemOr(args, 1, "deviceName", "BNS31954");
        boolean headed = Boolean.parseBoolean(System.getProperty("headed", "false"));

        try (MainframeDriver driver = MainframeDriver.builder()
                .host(host).port(23)
                .codePage("37")
                .deviceName(device)
                .headless(!headed)
                .defaultTimeout(Duration.ofSeconds(30))
                .build()) {

            System.out.println("Connecting to " + host + " (device=" + device + ", headed=" + headed + ") ...");
            driver.connect();

            System.out.println("Waiting for sign-on screen to be ready ...");
            driver.waitFor(MainframeConditions.inputReady());

            System.out.println("---- Sign-on screen ----");
            for (String row : driver.getScreen().allRows()) {
                System.out.println(row);
            }

            driver.screenshot(Paths.get("signon.png"));
            System.out.println("Saved screenshot: signon.png");

            // Demonstrate locator usage without actually submitting credentials.
            try {
                System.out.println("First input field: " + driver.findField(By.firstInputField()));
            } catch (RuntimeException e) {
                System.out.println("Could not locate first input field: " + e.getMessage());
            }
        }
        System.out.println("Done.");
    }

    private static String systemOr(String[] args, int idx, String prop, String fallback) {
        if (args != null && args.length > idx && args[idx] != null && !args[idx].isEmpty()) {
            return args[idx];
        }
        String sys = System.getProperty(prop);
        return (sys != null && !sys.isEmpty()) ? sys : fallback;
    }
}
