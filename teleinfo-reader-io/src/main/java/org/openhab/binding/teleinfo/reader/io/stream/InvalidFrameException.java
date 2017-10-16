package org.openhab.binding.teleinfo.reader.io.stream;

import java.util.Date;

public class InvalidFrameException extends Exception {

    private static final long serialVersionUID = 4729529258857792922L;

    private Date timestamp;

    public InvalidFrameException() {
        timestamp = new Date();
    }

    public InvalidFrameException(String message) {
        super(message, null);
    }

    public InvalidFrameException(String message, Throwable cause) {
        super(message, cause);
        timestamp = new Date();
    }

    public Date getTimestamp() {
        return timestamp;
    }

}
