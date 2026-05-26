package com.bns.etbic.craft.mainframe.pages;

import com.bns.etbic.craft.mainframe.elements.ScreenSnapshot;

/** Página genérica: envuelve una pantalla capturada para hacer aserciones. */
public final class MainframeScreen {

    private final ScreenSnapshot snapshot;

    public MainframeScreen(ScreenSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public boolean contains(String text) {
        return snapshot.contains(text);
    }

    public String text() {
        return snapshot.allText();
    }

    public ScreenSnapshot snapshot() {
        return snapshot;
    }
}
