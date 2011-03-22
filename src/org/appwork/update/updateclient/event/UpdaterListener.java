package org.appwork.update.updateclient.event;

import java.util.EventListener;

import org.appwork.update.updateclient.UpdaterState;

public interface UpdaterListener extends EventListener {

    void onStateEnter(UpdaterState state);

    void onStateExit(UpdaterState event);

    void onUpdaterEvent(UpdaterEvent event);

    void onUpdaterModuleEnd(UpdaterEvent event);

    void onUpdaterModuleProgress(UpdaterEvent event, int parameter);

    void onUpdaterModuleStart(UpdaterEvent event);

}
