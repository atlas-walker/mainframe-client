package org.tn5250j.mainframe.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.tn5250j.event.ScreenListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.mainframe.elements.ScreenSnapshot;
import org.tn5250j.mainframe.screenshot.ScreenshotRenderer;

public final class HeadedWindow implements ScreenListener {

    private final Screen5250 screen;
    private final ScreenshotRenderer renderer;
    private final JFrame frame;
    private final RenderPanel panel;

    public HeadedWindow(String title, Screen5250 screen, ScreenshotRenderer renderer) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("Cannot open headed window in headless JVM");
        }
        this.screen = screen;
        this.renderer = renderer;
        this.panel = new RenderPanel();
        this.frame = new JFrame(title);
        this.frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.frame.setLayout(new BorderLayout());
        this.frame.add(panel, BorderLayout.CENTER);
        BufferedImage initial = renderer.render(ScreenSnapshot.take(screen));
        panel.setImage(initial);
        Dimension size = new Dimension(initial.getWidth(), initial.getHeight());
        panel.setPreferredSize(size);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        screen.addScreenListener(this);
    }

    public void close() {
        screen.removeScreenListener(this);
        SwingUtilities.invokeLater(frame::dispose);
    }

    @Override
    public void onScreenChanged(int inUpdate, int startRow, int startCol, int endRow, int endCol) {
        repaintAsync();
    }

    @Override
    public void onScreenSizeChanged(int rows, int cols) {
        repaintAsync();
    }

    private void repaintAsync() {
        SwingUtilities.invokeLater(() -> {
            BufferedImage img = renderer.render(ScreenSnapshot.take(screen));
            panel.setImage(img);
            Dimension size = new Dimension(img.getWidth(), img.getHeight());
            if (!size.equals(panel.getPreferredSize())) {
                panel.setPreferredSize(size);
                frame.pack();
            }
            panel.repaint();
        });
    }

    private static final class RenderPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private volatile BufferedImage image;
        void setImage(BufferedImage image) { this.image = image; }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage img = this.image;
            if (img != null) {
                g.drawImage(img, 0, 0, null);
            }
        }
    }
}
