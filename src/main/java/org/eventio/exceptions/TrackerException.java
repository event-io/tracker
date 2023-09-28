package org.eventio.exceptions;

public class TrackerException extends Exception {

    public TrackerException() {
        super();
    }

    public TrackerException(String message) {
        super(message);
    }

    public TrackerException(String message, Throwable cause) {
        super(message, cause);
    }

}
