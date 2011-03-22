package org.appwork.update.updateclient.http;

public class ClientUpdateRequiredException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String      url;
    private final String      hash;

    public ClientUpdateRequiredException(final String url, final String hash) {
        this.url = url;
        this.hash = hash;

    }

    public String getHash() {
        return hash;
    }

    public String getUrl() {
        return url;
    }

}
