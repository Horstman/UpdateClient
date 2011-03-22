package org.appwork.update.exchange;

import org.appwork.storage.Storable;
import org.appwork.utils.logging.Log;

public class Branch implements Storable {
    private String name;

    @SuppressWarnings("unused")
    private Branch() {
        // required vor Storable interface
    }

    public Branch(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        Log.exception(new Exception());
        return getName();
    }
}
