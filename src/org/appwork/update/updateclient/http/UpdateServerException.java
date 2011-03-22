package org.appwork.update.updateclient.http;

import org.appwork.update.exchange.ServerError;

public class UpdateServerException extends Exception {

    private final ServerError type;

    public UpdateServerException(final ServerError valueOf) {
        this.type = valueOf;
    }

    public ServerError getType() {
        return this.type;
    }

}
