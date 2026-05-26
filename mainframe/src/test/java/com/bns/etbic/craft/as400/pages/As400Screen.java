package com.bns.etbic.craft.as400.pages;

import com.bns.etbic.craft.as400.elements.ScreenSnapshot;

/** Página genérica: envuelve una pantalla capturada para hacer aserciones. */
public final class As400Screen {

    private final ScreenSnapshot snapshot;

    public As400Screen(ScreenSnapshot snapshot) {
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
