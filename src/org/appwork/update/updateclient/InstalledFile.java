package org.appwork.update.updateclient;

import org.appwork.storage.JSonStorage;

public class InstalledFile {
    private String relPath;
    private long   lastMod;

    public InstalledFile() {

    }

    public InstalledFile(final String rel, final long lastMod) {
        this.lastMod = lastMod;
        this.relPath = rel;
    }

    // required to remove dupes
    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof InstalledFile)) { return false; }
        return this.hashCode() == o.hashCode();
    }

    public long getLastMod() {
        return this.lastMod;
    }

    public String getRelPath() {
        return this.relPath;
    }

    // required to remove dupes
    @Override
    public int hashCode() {
        return this.relPath.hashCode();
    }

    public void setLastMod(final long lastMod) {
        this.lastMod = lastMod;
    }

    public void setRelPath(final String relPath) {
        this.relPath = relPath;
    }

    public String toString() {
        return JSonStorage.toString(this);
    }
}
