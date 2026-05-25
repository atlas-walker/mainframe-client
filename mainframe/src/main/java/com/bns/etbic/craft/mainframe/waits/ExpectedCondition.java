package com.bns.etbic.craft.mainframe.waits;

import com.bns.etbic.craft.mainframe.elements.ScreenSnapshot;

@FunctionalInterface
public interface ExpectedCondition<T> {

    T apply(ScreenSnapshot snapshot);

    default String describe() {
        return getClass().getSimpleName();
    }
}
