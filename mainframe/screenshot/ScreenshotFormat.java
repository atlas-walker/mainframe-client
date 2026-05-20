package com.bns.etbic.craft.mainframe.screenshot;

public enum ScreenshotFormat {
    PNG("png"),
    JPEG("jpg");

    public final String extension;

    ScreenshotFormat(String extension) {
        this.extension = extension;
    }
}
