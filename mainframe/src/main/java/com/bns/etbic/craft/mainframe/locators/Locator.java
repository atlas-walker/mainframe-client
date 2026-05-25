package com.bns.etbic.craft.mainframe.locators;

import java.util.Collections;
import java.util.List;

public interface Locator<T> {

    T locate(LocatorContext ctx);

    default List<T> locateAll(LocatorContext ctx) {
        T single = locate(ctx);
        return single == null ? Collections.emptyList() : Collections.singletonList(single);
    }

    default String describe() {
        return getClass().getSimpleName();
    }
}
