package org.appwork.update.exchange;

public class PackageException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final ServerError type;

    public PackageException(final ServerError unknownApp) {
        super(unknownApp.name());
        this.type = unknownApp;
    }

    public PackageException(final String string) {
        super(string);
        this.type = null;
    }

    public ServerError getType() {
        return this.type;
    }

}
