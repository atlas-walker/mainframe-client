package com.bns.etbic.craft.as400;

/**
 * Unchecked exception raised for any AS/400 (IBM i) interaction failure: connection
 * problems, invalid configuration, missing fields, or timeouts while waiting for the
 * host.
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public class As400Exception extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the given message.
     *
     * @param message the detail message
     */
    public As400Exception(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given message and underlying cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public As400Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
