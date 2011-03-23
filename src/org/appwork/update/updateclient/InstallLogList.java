package org.appwork.update.updateclient;

import java.util.ArrayList;

public class InstallLogList extends ArrayList<InstalledFile> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private boolean           changed          = false;

    public InstallLogList() {
        super();
    }

    public boolean isChanged() {
        return this.changed;
    }

    public void setChanged(final boolean changed) {
        this.changed = changed;
    }
}
