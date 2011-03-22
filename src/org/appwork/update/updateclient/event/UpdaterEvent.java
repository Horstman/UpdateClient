package org.appwork.update.updateclient.event;

import org.appwork.update.updateclient.Updater;
import org.appwork.utils.event.SimpleEvent;

public class UpdaterEvent extends SimpleEvent<Updater, Object, UpdaterEvent.Types> {

    public enum Types {

        /**
         * Parameter[0]=(String)branchname
         */
        BRANCH_UPDATED,
        /**
         * Parameter[0]=(Mirror)file
         */
        END_DOWNLOAD_FILE,

        END_FILELIST_UPDATE,
        END_FILTERING,
        END_REPO_UPDATE,
        /**
         * Parameter[0]=(int) errorcounter files which could not be reverted
         */
        END_REVERT,
        EXIT_REQUEST,
        /**
         * Parameter[0]=(Throwable) exception
         */
        INSTALL_FAILED,
        /**
         * Parameter[0]=(File) lockedfile
         */
        LOCKED,
        UNLOCKED,
        /**
         * Parameter[0]=(int) Percent 0-100
         */

        PROGRESS_DOWNLOAD_FILE,
        /**
         * Parameter[0]=(int) Percent 0-100
         */

        PROGRESS_INSTALL,
        /**
         * Parameter[0]=(int) Percent 0-100
         */

        PROGRESS_REVERT,
        /**
         * Parameter[0]=(Mirror)file
         */
        START_DOWNLOAD_FILE,

        START_FILELIST_UPDATE,

        START_FILTERING,
        START_REPO_UPDATE,
        START_REVERT,

        /**
         * Parameter[0]=(InstalledFile) removed file
         * 
         */
        DELETED_FILE,
        /**
         * Parameter[0]: (File) Final File<br>
         * Parameter[1]: (String) relative Path
         */
        START_INSTALL_FILE,

        /**
         * Parameter[0]: (File) Final File<br>
         * Parameter[1]: (String) relative Path
         */
        END_INSTALL_FILE,

        /**
         * No parameters
         */
        PROGRESS_SERVERLIST,

        /**
         * Parameter[0]=(int) Percent 0-100
         */

        PROGRESS_FILTER,
        /**
         * Parameter[0]=(String)old branch
         */

        BRANCH_RESET,

        /**
         * Parameter[0]=(UpdateState)state
         */
        STATE_ENTER,
        /**
         * Parameter[0]=(UpdateState)state
         */
        STATE_EXIT

    }

    public UpdaterEvent(final Updater caller, final Types type, final Object... parameters) {
        super(caller, type, parameters);
    }

}
