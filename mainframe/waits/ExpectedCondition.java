package org.tn5250j.mainframe.waits;

import org.tn5250j.mainframe.elements.ScreenSnapshot;

@FunctionalInterface
public interface ExpectedCondition<T> {

    T apply(ScreenSnapshot snapshot);

    default String describe() {
        return getClass().getSimpleName();
    }
}
