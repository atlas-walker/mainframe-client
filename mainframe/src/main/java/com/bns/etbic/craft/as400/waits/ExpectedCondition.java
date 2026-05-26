package com.bns.etbic.craft.as400.waits;

import com.bns.etbic.craft.as400.elements.ScreenSnapshot;

@FunctionalInterface
public interface ExpectedCondition<T> {

    T apply(ScreenSnapshot snapshot);

    default String describe() {
        return getClass().getSimpleName();
    }
}
