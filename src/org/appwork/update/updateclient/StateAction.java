package org.appwork.update.updateclient;

import org.appwork.controlling.State;

public abstract class StateAction extends State {

    public StateAction(final String name) {
        super(name);
    }

    public abstract void prepare();

    public abstract StateAction run() throws Exception;
}
