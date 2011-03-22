package org.appwork.update.updateclient;

import java.io.File;

public class LockedException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final File        file;

    public LockedException(final File f) {
        super("Locked");
        this.file = f;
    }

    public File getFile() {
        return this.file;
    }

}
