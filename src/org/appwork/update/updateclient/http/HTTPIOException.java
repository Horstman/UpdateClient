package org.appwork.update.updateclient.http;

public class HTTPIOException extends Exception {

    private int responseCode = -1;

    public HTTPIOException(final int responseCode, final String message) {
        super(message);
        this.responseCode = responseCode;
    }

    // public HTTPIOException(final String message) {
    // super(message);
    // }
    //
    // public HTTPIOException(final String message, final Throwable cause) {
    // super(message, cause);
    //
    // }

    public HTTPIOException(final Throwable cause) {
        super(cause.getLocalizedMessage(), cause);

    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
    }

}
