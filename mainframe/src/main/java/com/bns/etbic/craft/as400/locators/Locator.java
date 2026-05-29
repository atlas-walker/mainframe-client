package com.bns.etbic.craft.as400.locators;

import java.util.Collections;
import java.util.List;

/**
 * Strategy that resolves one or more elements of type {@code T} (a field or a text
 * region) against a {@link LocatorContext}. See {@link By} for the built-in factories.
 *
 * @param <T> the type of element this locator resolves
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public interface Locator<T> {

    /**
     * Resolves the single matching element.
     *
     * @param ctx the screen context to search
     * @return the matching element
     */
    T locate(LocatorContext ctx);

    /**
     * Resolves every matching element. The default implementation wraps the result
     * of {@link #locate(LocatorContext)} into a list.
     *
     * @param ctx the screen context to search
     * @return all matching elements, or an empty list when none match
     */
    default List<T> locateAll(LocatorContext ctx) {
        T single = locate(ctx);
        return single == null ? Collections.emptyList() : Collections.singletonList(single);
    }

    /**
     * Describes this locator for diagnostics.
     *
     * @return a human-readable description, used in error messages
     */
    default String describe() {
        return getClass().getSimpleName();
    }
}
