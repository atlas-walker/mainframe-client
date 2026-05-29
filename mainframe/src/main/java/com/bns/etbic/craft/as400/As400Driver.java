package com.bns.etbic.craft.as400;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import javax.imageio.ImageIO;

import org.tn5250j.Session5250;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenFields;
import com.bns.etbic.craft.as400.elements.FieldActions;
import com.bns.etbic.craft.as400.elements.As400Field;
import com.bns.etbic.craft.as400.elements.ScreenRegion;
import com.bns.etbic.craft.as400.elements.ScreenSnapshot;
import com.bns.etbic.craft.as400.internal.ScreenSync;
import com.bns.etbic.craft.as400.keys.Key;
import com.bns.etbic.craft.as400.locators.Locator;
import com.bns.etbic.craft.as400.locators.LocatorContext;
import com.bns.etbic.craft.as400.screenshot.ScreenshotFormat;
import com.bns.etbic.craft.as400.screenshot.ScreenshotRenderer;
import com.bns.etbic.craft.as400.transport.SessionFactory;
import com.bns.etbic.craft.as400.ui.HeadedWindow;
import com.bns.etbic.craft.as400.waits.ExpectedCondition;

public final class As400Driver implements AutoCloseable {

    private final As400Config config;
    private final ScreenshotRenderer renderer;

    private Session5250 session;
    private Screen5250 screen;
    private ScreenSync sync;
    private HeadedWindow headed;
    private boolean connected;

    public As400Driver(As400Config config) {
        this(config, new ScreenshotRenderer());
    }

    public As400Driver(As400Config config, ScreenshotRenderer renderer) {
        this.config = config;
        this.renderer = renderer;
    }

    public synchronized void connect() {
        if (connected) return;
        session = SessionFactory.open(config);
        session.connect();
        screen = session.getScreen();
        sync = new ScreenSync(screen);
        if (!config.headless()) {
            headed = new HeadedWindow(config.sessionName() + " — " + config.host(), session);
        }
        connected = true;
    }

    public synchronized void disconnect() {
        if (!connected) return;
        try {
            if (sync != null) {
                sync.detach();
                sync = null;
            }
            if (session != null) {
                session.disconnect();
            }
            if (headed != null) {
                headed.markSessionEnded();
            }
        } finally {
            connected = false;
            session = null;
            screen = null;
        }
    }

    /**
     * Programmatically dispose the headed window if one is open. Safe to call
     * even in headless mode (no-op).
     */
    public void closeHeadedWindow() {
        if (headed != null) {
            headed.close();
            headed = null;
        }
    }

    /**
     * Block until the headed window is disposed by the user (or by
     * {@link #closeHeadedWindow()}). No-op in headless mode.
     */
    public void awaitHeadedWindowClose() {
        if (headed == null) return;
        try {
            headed.awaitClose();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * True when running in headed mode and the window has not been disposed.
     */
    public boolean isHeadedWindowOpen() {
        return headed != null && !headed.isDisposed();
    }

    public boolean isConnected() {
        return connected && session != null && session.isConnected();
    }

    @Override
    public void close() {
        disconnect();
    }

    public ScreenSnapshot getScreen() {
        ensureConnected();
        return ScreenSnapshot.take(screen);
    }

    public String screenAsText() {
        return getScreen().allText();
    }

    public int cursorRow() {
        ensureConnected();
        return screen.getCurrentRow();
    }

    public int cursorCol() {
        ensureConnected();
        return screen.getCurrentCol();
    }

    public <T> T find(Locator<T> by) {
        ensureConnected();
        return by.locate(currentContext());
    }

    public <T> List<T> findAll(Locator<T> by) {
        ensureConnected();
        return by.locateAll(currentContext());
    }

    public As400Field findField(Locator<As400Field> by) {
        return find(by);
    }

    public ScreenRegion findText(Locator<ScreenRegion> by) {
        return find(by);
    }

    public void type(String text) {
        ensureConnected();
        screen.sendKeys(text);
    }

    public void typeAt(int row, int col, String text) {
        ensureConnected();
        screen.setCursor(row, col);
        screen.sendKeys(text);
    }

    public void press(Key key) {
        ensureConnected();
        screen.sendKeys(key.mnemonic());
    }

    public void press(Key... keys) {
        ensureConnected();
        StringBuilder sb = new StringBuilder();
        for (Key k : keys) {
            sb.append(k.token());
        }
        screen.sendKeys(sb.toString());
    }

    /**
     * Presiona una tecla y espera a que el HOST responda. Captura la pantalla
     * actual, envía la tecla y bloquea hasta que el host repinta (un cambio de
     * pantalla posterior al envío) y el teclado vuelve a quedar listo para entrada.
     *
     * <p>Es la espera correcta cuando NO sabes qué texto traerá la siguiente
     * pantalla: a diferencia de {@code Thread.sleep} (que puede leer la pantalla
     * vieja) o de {@code screenStable} (que puede declarar "estable" la pantalla
     * previa si el host tarda), aquí se ancla al evento real de repintado.
     *
     * @return el snapshot de la pantalla ya repintada y lista.
     */
    public ScreenSnapshot pressAndWait(Key key) {
        return pressAndWait(key, config.defaultTimeout());
    }

    public ScreenSnapshot pressAndWait(Key key, Duration timeout) {
        ensureConnected();
        long baseline = sync.lastChangeNanos();
        press(key);

        long deadline = System.nanoTime() + timeout.toNanos();
        long lastSeen = baseline;
        while (true) {
            ScreenSnapshot snap = ScreenSnapshot.take(screen);
            boolean hostResponded = sync.lastChangeNanos() > baseline;
            if (hostResponded && snap.inputReady()) {
                return snap;
            }
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                throw new As400Exception(
                    "Timed out after " + timeout.toMillis() + "ms waiting for the host to "
                        + "respond after pressing " + key + ". Last screen:\n" + snap.allText());
            }
            long waitMs = Math.min(250, Math.max(20, remaining / 1_000_000L));
            sync.awaitChangeSince(lastSeen, waitMs);
            lastSeen = sync.lastChangeNanos();
        }
    }

    public <T> T waitFor(ExpectedCondition<T> condition) {
        return waitFor(condition, config.defaultTimeout());
    }

    public <T> T waitFor(ExpectedCondition<T> condition, Duration timeout) {
        ensureConnected();
        long deadline = System.nanoTime() + timeout.toNanos();
        long lastSeen = sync.lastChangeNanos() - 1;

        while (true) {
            ScreenSnapshot snap = ScreenSnapshot.take(screen);
            T result = condition.apply(snap);
            if (result != null && !Boolean.FALSE.equals(result)) {
                return result;
            }
            long now = System.nanoTime();
            long remaining = deadline - now;
            if (remaining <= 0) {
                throw new As400Exception(
                    "Timed out after " + timeout.toMillis() + "ms waiting for " + condition.describe()
                        + ". Last screen:\n" + snap.allText());
            }
            long waitMs = Math.min(250, Math.max(20, remaining / 1_000_000L));
            sync.awaitChangeSince(lastSeen, waitMs);
            lastSeen = sync.lastChangeNanos();
        }
    }

    public BufferedImage screenshot() {
        return renderer.render(getScreen());
    }

    public Path screenshot(Path file) {
        return screenshot(file, ScreenshotFormat.PNG);
    }

    public Path screenshot(Path file, ScreenshotFormat format) {
        BufferedImage img = screenshot();
        try {
            File out = file.toFile();
            File parent = out.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            ImageIO.write(img, format.extension, out);
            return file;
        } catch (IOException e) {
            throw new As400Exception("Failed to write screenshot to " + file, e);
        }
    }

    public Session5250 rawSession() {
        ensureConnected();
        return session;
    }

    public Screen5250 rawScreen() {
        ensureConnected();
        return screen;
    }

    private void ensureConnected() {
        if (!connected) {
            throw new As400Exception("Driver not connected. Call connect() first.");
        }
    }

    private LocatorContext currentContext() {
        final ScreenSnapshot snap = ScreenSnapshot.take(screen);
        final FieldActions actions = new FieldActionsImpl();
        final Screen5250 s = this.screen;
        return new LocatorContext() {
            @Override public Screen5250 screen()           { return s; }
            @Override public ScreenSnapshot snapshot()      { return snap; }
            @Override public FieldActions fieldActions()    { return actions; }
        };
    }

    private final class FieldActionsImpl implements FieldActions {
        @Override
        public void typeAt(int row, int col, String text) {
            As400Driver.this.typeAt(row, col, text);
        }

        @Override
        public void clearField(int row, int col, int length) {
            ensureConnected();
            ScreenFields fields = screen.getScreenFields();
            ScreenField target = (fields == null) ? null : fields.findByPosition(row - 1, col - 1);
            screen.setCursor(row, col);
            if (target != null) {
                screen.sendKeys(org.tn5250j.keyboard.KeyMnemonic.ERASE_EOF);
            } else {
                StringBuilder spaces = new StringBuilder(length);
                for (int i = 0; i < length; i++) spaces.append(' ');
                screen.sendKeys(spaces.toString());
                screen.setCursor(row, col);
            }
        }
    }
}
