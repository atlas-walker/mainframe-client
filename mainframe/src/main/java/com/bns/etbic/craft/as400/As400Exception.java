package com.bns.etbic.craft.as400;

public class As400Exception extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public As400Exception(String message) {
        super(message);
    }

    public As400Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
