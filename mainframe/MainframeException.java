package org.tn5250j.mainframe;

public class MainframeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MainframeException(String message) {
        super(message);
    }

    public MainframeException(String message, Throwable cause) {
        super(message, cause);
    }
}
