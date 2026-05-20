package org.tn5250j.mainframe;

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
import org.tn5250j.mainframe.elements.FieldActions;
import org.tn5250j.mainframe.elements.MainframeField;
import org.tn5250j.mainframe.elements.ScreenRegion;
import org.tn5250j.mainframe.elements.ScreenSnapshot;
import org.tn5250j.mainframe.internal.ScreenSync;
import org.tn5250j.mainframe.keys.Key;
import org.tn5250j.mainframe.locators.Locator;
import org.tn5250j.mainframe.locators.LocatorContext;
import org.tn5250j.mainframe.screenshot.ScreenshotFormat;
import org.tn5250j.mainframe.screenshot.ScreenshotRenderer;
import org.tn5250j.mainframe.transport.SessionFactory;
import org.tn5250j.mainframe.ui.HeadedWindow;
import org.tn5250j.mainframe.waits.ExpectedCondition;

public final class MainframeDriver implements AutoCloseable {

    private final MainframeOptions options;
    private final ScreenshotRenderer renderer;

    private Session5250 session;
    private Screen5250 screen;
    private ScreenSync sync;
    private HeadedWindow headed;
    private boolean connected;

    private MainframeDriver(MainframeOptions options, ScreenshotRenderer renderer) {
        this.options = options;
        this.renderer = renderer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public synchronized void connect() {
        if (connected) return;
        session = SessionFactory.open(options);
        session.connect();
        screen = session.getScreen();
        sync = new ScreenSync(screen);
        if (!options.headless()) {
            headed = new HeadedWindow(options.sessionName() + " — " + options.host(), screen, renderer);
        }
        connected = true;
    }

    public synchronized void disconnect() {
        if (!connected) return;
        try {
            if (headed != null) {
                headed.close();
                headed = null;
            }
            if (sync != null) {
                sync.detach();
                sync = null;
            }
            if (session != null) {
                session.disconnect();
            }
        } finally {
            connected = false;
            session = null;
            screen = null;
        }
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

    public MainframeField findField(Locator<MainframeField> by) {
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

    public <T> T waitFor(ExpectedCondition<T> condition) {
        return waitFor(condition, options.defaultTimeout());
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
                throw new MainframeException(
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
            throw new MainframeException("Failed to write screenshot to " + file, e);
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
            throw new MainframeException("Driver not connected. Call connect() first.");
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
            MainframeDriver.this.typeAt(row, col, text);
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

    public static final class Builder {
        private MainframeOptions.Builder optsBuilder = MainframeOptions.builder();
        private ScreenshotRenderer renderer;

        public Builder host(String host)              { optsBuilder.host(host); return this; }
        public Builder port(int port)                 { optsBuilder.port(port); return this; }
        public Builder sslType(String sslType)        { optsBuilder.sslType(sslType); return this; }
        public Builder codePage(String codePage)      { optsBuilder.codePage(codePage); return this; }
        public Builder deviceName(String deviceName)  { optsBuilder.deviceName(deviceName); return this; }
        public Builder screenSize(MainframeOptions.ScreenSize sz) { optsBuilder.screenSize(sz); return this; }
        public Builder enhancedTn(boolean on)         { optsBuilder.enhancedTn(on); return this; }
        public Builder headless(boolean on)           { optsBuilder.headless(on); return this; }
        public Builder headed()                       { optsBuilder.headed(); return this; }
        public Builder defaultTimeout(Duration t)     { optsBuilder.defaultTimeout(t); return this; }
        public Builder sessionName(String name)       { optsBuilder.sessionName(name); return this; }
        public Builder renderer(ScreenshotRenderer r) { this.renderer = r; return this; }

        public Builder options(MainframeOptions opts) {
            this.optsBuilder = MainframeOptions.builder()
                .host(opts.host()).port(opts.port())
                .sslType(opts.sslType()).codePage(opts.codePage())
                .deviceName(opts.deviceName()).screenSize(opts.screenSize())
                .enhancedTn(opts.enhancedTn()).headless(opts.headless())
                .defaultTimeout(opts.defaultTimeout()).sessionName(opts.sessionName());
            return this;
        }

        public MainframeDriver build() {
            MainframeOptions opts = optsBuilder.build();
            ScreenshotRenderer r = (renderer != null) ? renderer : new ScreenshotRenderer();
            return new MainframeDriver(opts, r);
        }
    }
}
