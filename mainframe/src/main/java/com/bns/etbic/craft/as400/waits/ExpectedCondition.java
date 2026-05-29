package com.bns.etbic.craft.as400.waits;

import com.bns.etbic.craft.as400.elements.ScreenSnapshot;

/**
 * A condition polled against successive screen snapshots while waiting for the host.
 *
 * <p>{@link #apply(ScreenSnapshot)} returns {@code null} (or {@link Boolean#FALSE})
 * while the condition is not yet met, and a non-null, non-false value once it is; the
 * driver returns that value from its {@code waitFor} methods. See {@link As400Conditions}
 * for the built-in conditions.
 *
 * @param <T> the value produced when the condition is met
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
@FunctionalInterface
public interface ExpectedCondition<T> {

    /**
     * Evaluates the condition against a screen snapshot.
     *
     * @param snapshot the current screen snapshot
     * @return the result when the condition is met, otherwise {@code null}
     */
    T apply(ScreenSnapshot snapshot);

    /**
     * Describes this condition for diagnostics.
     *
     * @return a human-readable description, used in timeout messages
     */
    default String describe() {
        return getClass().getSimpleName();
    }
}
