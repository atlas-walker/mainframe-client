package com.bns.etbic.craft.mainframe.ui;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.tn5250j.Session5250;
import org.tn5250j.SessionPanel;

/**
 * Headed terminal window backed by tn5250j's real {@link SessionPanel} — fully
 * interactive (keyboard + mouse) and uses tn5250j's own repaint pipeline, so
 * local keystrokes from the driver and from the user appear immediately.
 *
 * <p>Lifecycle:
 * <ul>
 *   <li>The window does not auto-dispose when the driver disconnects. The user
 *       may keep inspecting the last screen state.</li>
 *   <li>Clicking the X button disposes the window.</li>
 *   <li>{@link #close()} programmatically disposes the window.</li>
 *   <li>{@link #awaitClose()} blocks until the window is disposed.</li>
 * </ul>
 */
public final class HeadedWindow {

    private final JFrame frame;
    private final SessionPanel panel;
    private final CountDownLatch closed = new CountDownLatch(1);
    private volatile boolean disposed;

    public HeadedWindow(String title, Session5250 session) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("Cannot open headed window in headless JVM");
        }
        this.panel = new SessionPanel(session);
        this.frame = buildFrame(title, panel);
    }

    private JFrame buildFrame(String title, SessionPanel panel) {
        final JFrame f = new JFrame(title);
        try {
            SwingUtilities.invokeAndWait(() -> {
                f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                f.setLayout(new BorderLayout());
                f.add(panel, BorderLayout.CENTER);
                f.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        disposed = true;
                        closed.countDown();
                    }
                });
                f.pack();
                f.setLocationByPlatform(true);
                f.setVisible(true);
                panel.requestFocusInWindow();
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize headed window", e);
        }
        return f;
    }

    /**
     * Marks the window as "session ended" by updating the title. Does not
     * dispose the window — the user may keep inspecting it.
     */
    public void markSessionEnded() {
        SwingUtilities.invokeLater(() -> {
            if (!disposed) {
                frame.setTitle(frame.getTitle() + "  [disconnected]");
            }
        });
    }

    public void close() {
        if (disposed) return;
        SwingUtilities.invokeLater(() -> {
            if (!disposed) {
                frame.dispose();
            }
        });
    }

    public void awaitClose() throws InterruptedException {
        closed.await();
    }

    public boolean isDisposed() {
        return disposed;
    }

    public SessionPanel panel() {
        return panel;
    }
}
