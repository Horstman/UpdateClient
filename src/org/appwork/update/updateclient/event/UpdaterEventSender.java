package org.appwork.update.updateclient.event;

import org.appwork.update.updateclient.UpdaterState;
import org.appwork.utils.event.Eventsender;

public class UpdaterEventSender extends Eventsender<UpdaterListener, UpdaterEvent> {

    @Override
    protected void fireEvent(final UpdaterListener listener, final UpdaterEvent event) {
        switch (event.getType()) {

            case START_FILELIST_UPDATE:
            case START_REPO_UPDATE:
            case START_FILTERING:
            case START_DOWNLOAD_FILE:
            case START_INSTALL_FILE:

            case START_REVERT:

                listener.onUpdaterModuleStart(event);
                break;

            case PROGRESS_DOWNLOAD_FILE:
            case PROGRESS_INSTALL:
            case PROGRESS_REVERT:
            case PROGRESS_SERVERLIST:
            case PROGRESS_FILTER:

                listener.onUpdaterModuleProgress(event, (Integer) event.getParameter());
                break;

            case END_FILELIST_UPDATE:
            case END_FILTERING:
            case END_INSTALL_FILE:
            case END_REPO_UPDATE:
            case END_DOWNLOAD_FILE:
            case END_REVERT:

                listener.onUpdaterModuleEnd(event);
                break;
            case STATE_ENTER:
                listener.onStateEnter((UpdaterState) event.getParameter());
                break;
            case STATE_EXIT:
                listener.onStateExit((UpdaterState) event.getParameter());
                break;
            default:
                listener.onUpdaterEvent(event);

        }
    }
}
