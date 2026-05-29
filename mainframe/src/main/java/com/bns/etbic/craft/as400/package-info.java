/**
 * Public API for driving AS/400 (IBM i) 5250 sessions.
 *
 * <p>The core types are {@link com.bns.etbic.craft.as400.As400Driver} (the live
 * session handle), {@link com.bns.etbic.craft.as400.As400Config} (its immutable
 * configuration), {@link com.bns.etbic.craft.as400.As400Factory} (shared driver
 * creation and teardown) and {@link com.bns.etbic.craft.as400.BasePage} (the base
 * class for screen Page Objects). Failures are reported as
 * {@link com.bns.etbic.craft.as400.As400Exception}.
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
package com.bns.etbic.craft.as400;
