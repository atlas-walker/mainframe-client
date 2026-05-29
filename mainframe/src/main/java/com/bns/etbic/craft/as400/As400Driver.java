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

/**
 * Live handle to an AS/400 (IBM i) 5250 session.
 *
 * <p>The driver owns the connection to the host and is the single object through
 * which callers send input ({@link #type}, {@link #press}), locate fields and text
 * ({@link #find}), wait for host responses ({@link #waitFor}, {@link #pressAndWait})
 * and capture the screen ({@link #getScreen}, {@link #screenshot}). It is mutable
 * and always reflects what the host is currently showing.
 *
 * <p>Configuration is supplied as an immutable {@link As400Config}. Most callers do
 * not instantiate the driver directly; they obtain the shared instance from
 * {@link As400Factory#getDriver()}.
 *
 * <p>Instances are {@link AutoCloseable}; {@link #close()} disconnects the session.
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public final class As400Driver implements AutoCloseable {

    private final As400Config config;
    private final ScreenshotRenderer renderer;

    private Session5250 session;
    private Screen5250 screen;
    private ScreenSync sync;
    private HeadedWindow headed;
    private boolean connected;

    /**
     * Creates a driver with the given configuration and a default screenshot
     * renderer. The connection is not opened until {@link #connect()} is called.
     *
     * @param config the connection configuration
     */
    public As400Driver(As400Config config) {
        this(config, new ScreenshotRenderer());
    }

    /**
     * Creates a driver with the given configuration and a custom screenshot
     * renderer. The connection is not opened until {@link #connect()} is called.
     *
     * @param config   the connection configuration
     * @param renderer the renderer used to turn screens into images
     */
    public As400Driver(As400Config config, ScreenshotRenderer renderer) {
        this.config = config;
        this.renderer = renderer;
    }

    /** Opens the session and, in headed mode, the terminal window. No-op if already connected. */
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

    /** Disconnects the session and releases resources. No-op if not connected. */
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
     * Tells whether a headed window is currently open.
     *
     * @return {@code true} when running in headed mode and the window has not been disposed
     */
    public boolean isHeadedWindowOpen() {
        return headed != null && !headed.isDisposed();
    }

    /**
     * Tells whether the driver is connected.
     *
     * @return {@code true} if the driver is connected and the session is live
     */
    public boolean isConnected() {
        return connected && session != null && session.isConnected();
    }

    /** Disconnects the session ({@link AutoCloseable} support). */
    @Override
    public void close() {
        disconnect();
    }

    /**
     * Takes an immutable snapshot of the current screen.
     *
     * @return a snapshot of the current screen
     */
    public ScreenSnapshot getScreen() {
        ensureConnected();
        return ScreenSnapshot.take(screen);
    }

    /**
     * Renders the current screen as text.
     *
     * @return the current screen as a single newline-separated string
     */
    public String screenAsText() {
        return getScreen().allText();
    }

    /**
     * Returns the current cursor row.
     *
     * @return the 1-based cursor row
     */
    public int cursorRow() {
        ensureConnected();
        return screen.getCurrentRow();
    }

    /**
     * Returns the current cursor column.
     *
     * @return the 1-based cursor column
     */
    public int cursorCol() {
        ensureConnected();
        return screen.getCurrentCol();
    }

    /**
     * Resolves a single element on the current screen.
     *
     * @param by  the locator
     * @param <T> the located element type
     * @return the matching element
     */
    public <T> T find(Locator<T> by) {
        ensureConnected();
        return by.locate(currentContext());
    }

    /**
     * Resolves every element matching a locator on the current screen.
     *
     * @param by  the locator
     * @param <T> the located element type
     * @return all matching elements
     */
    public <T> List<T> findAll(Locator<T> by) {
        ensureConnected();
        return by.locateAll(currentContext());
    }

    /**
     * Convenience for {@link #find(Locator)} returning a field.
     *
     * @param by the field locator
     * @return the matching field
     */
    public As400Field findField(Locator<As400Field> by) {
        return find(by);
    }

    /**
     * Convenience for {@link #find(Locator)} returning a text region.
     *
     * @param by the text locator
     * @return the matching text region
     */
    public ScreenRegion findText(Locator<ScreenRegion> by) {
        return find(by);
    }

    /**
     * Sends text at the current cursor position.
     *
     * @param text the text to type
     */
    public void type(String text) {
        ensureConnected();
        screen.sendKeys(text);
    }

    /**
     * Moves the cursor to a position and sends text there.
     *
     * @param row  the 1-based row
     * @param col  the 1-based column
     * @param text the text to type
     */
    public void typeAt(int row, int col, String text) {
        ensureConnected();
        screen.setCursor(row, col);
        screen.sendKeys(text);
    }

    /**
     * Sends a single key.
     *
     * @param key the key to press
     */
    public void press(Key key) {
        ensureConnected();
        screen.sendKeys(key.mnemonic());
    }

    /**
     * Sends several keys in sequence.
     *
     * @param keys the keys to press, in order
     */
    public void press(Key... keys) {
        ensureConnected();
        StringBuilder sb = new StringBuilder();
        for (Key k : keys) {
            sb.append(k.token());
        }
        screen.sendKeys(sb.toString());
    }

    /**
     * Presses a key and waits for the host to respond, using the configured default
     * timeout. Captures the current screen, sends the key, and blocks until the host
     * repaints (a screen change after the send) and the keyboard is ready for input
     * again.
     *
     * <p>This is the correct wait when the next screen's text is unknown: unlike
     * {@code Thread.sleep} (which may read the stale screen) or {@code screenStable}
     * (which may declare the previous screen "stable" if the host is slow), it anchors
     * to the real repaint event.
     *
     * @param key the key to press
     * @return the snapshot of the repainted, ready screen
     * @throws As400Exception if the host does not respond within the timeout
     */
    public ScreenSnapshot pressAndWait(Key key) {
        return pressAndWait(key, config.defaultTimeout());
    }

    /**
     * Presses a key and waits for the host to respond, up to the given timeout.
     *
     * @param key     the key to press
     * @param timeout the maximum time to wait for the host
     * @return the snapshot of the repainted, ready screen
     * @throws As400Exception if the host does not respond within {@code timeout}
     */
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

    /**
     * Waits for a condition to be met, using the configured default timeout.
     *
     * @param condition the condition to wait for
     * @param <T>       the value produced when the condition is met
     * @return the condition's result
     * @throws As400Exception if the condition is not met within the timeout
     */
    public <T> T waitFor(ExpectedCondition<T> condition) {
        return waitFor(condition, config.defaultTimeout());
    }

    /**
     * Waits for a condition to be met, up to the given timeout.
     *
     * @param condition the condition to wait for
     * @param timeout   the maximum time to wait
     * @param <T>       the value produced when the condition is met
     * @return the condition's result
     * @throws As400Exception if the condition is not met within {@code timeout}
     */
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

    /**
     * Renders an image of the current screen.
     *
     * @return an image of the current screen
     */
    public BufferedImage screenshot() {
        return renderer.render(getScreen());
    }

    /**
     * Writes a PNG screenshot of the current screen to a file, creating parent
     * directories as needed.
     *
     * @param file the destination file
     * @return {@code file}, for convenience
     * @throws As400Exception if the image cannot be written
     */
    public Path screenshot(Path file) {
        return screenshot(file, ScreenshotFormat.PNG);
    }

    /**
     * Writes a screenshot of the current screen to a file in the given format,
     * creating parent directories as needed.
     *
     * @param file   the destination file
     * @param format the image format
     * @return {@code file}, for convenience
     * @throws As400Exception if the image cannot be written
     */
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

    /**
     * Exposes the underlying tn5250j session for operations not covered by the driver.
     *
     * @return the underlying tn5250j session
     */
    public Session5250 rawSession() {
        ensureConnected();
        return session;
    }

    /**
     * Exposes the underlying tn5250j screen for operations not covered by the driver.
     *
     * @return the underlying tn5250j screen
     */
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
