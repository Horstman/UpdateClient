package org.appwork.update.updateclient;

public abstract class UpdaterState extends StateAction {

    public UpdaterState() {
        super("UpdaterState");

    }

    public abstract int getProgress();

    @Override
    public String toString() {
        return "UpdaterState_" + getClass().getSimpleName();
    }

}
