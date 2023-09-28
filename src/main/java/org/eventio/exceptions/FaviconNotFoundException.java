package org.eventio.exceptions;

public class FaviconNotFoundException extends TrackerException {

    public FaviconNotFoundException() {
        super();
    }

    public FaviconNotFoundException(String message) {
        super(message);
    }

    public FaviconNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
