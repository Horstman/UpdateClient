package org.appwork.update.exchange;

public enum UpdateFlag {
    /**
     * do not update on MAC Systems
     */

    NOT_MAC,
    /**
     * do not update on windows systems
     */
    NOT_WIN,
    /**
     * Do not update on linux systems
     */
    NOT_LIN,
    /**
     * only update existing files
     */
    ONLY_IF_EXISTS,
    /**
     * Update files al,though the have an .noupdate file
     */
    FORCE,
}
