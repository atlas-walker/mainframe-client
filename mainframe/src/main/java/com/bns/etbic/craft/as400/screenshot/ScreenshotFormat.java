package com.bns.etbic.craft.as400.screenshot;

/**
 * Image formats supported when writing a screenshot to disk.
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public enum ScreenshotFormat {

    /** Lossless PNG ({@code .png}). */
    PNG("png"),

    /** Lossy JPEG ({@code .jpg}). */
    JPEG("jpg");

    /** File extension and {@code ImageIO} format name for this format. */
    public final String extension;

    ScreenshotFormat(String extension) {
        this.extension = extension;
    }
}
