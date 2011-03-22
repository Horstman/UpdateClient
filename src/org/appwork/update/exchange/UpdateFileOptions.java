package org.appwork.update.exchange;

import java.util.ArrayList;

import org.appwork.storage.Storable;

public class UpdateFileOptions implements Storable {
    /**
     * The file is a windows file. Default:true;
     */
    private boolean           windows      = true;
    /**
     * Can be used for optional files to install them updater.jar -install <id><br>
     * Moreover, this id is used in {@link #parentIDList}
     */
    private String            id           = null;
    /**
     * This is a linux file. Default:true
     */
    private boolean           linux        = true;
    /**
     * If you set up a list here, this file will be install only if one or more
     * "Required by" files are installed <br>
     * This list has to contain a list of ids!
     */
    private ArrayList<String> parentIDList = new ArrayList<String>();
    /**
     * this is a mac file. Default:true
     */
    private boolean           mac          = true;
    /**
     * This file will only be updated if it exists.
     */
    private boolean           onlyIfExists = false;

    public UpdateFileOptions() {
        // keep this for Storable INterface
    }

    /**
     * @see #id
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * @see #parentIDList
     * @return
     */
    public ArrayList<String> getParentIDList() {
        return this.parentIDList;
    }

    /**
     * @see #linux
     * @return
     */
    public boolean isLinux() {
        return this.linux;
    }

    /**
     * @see #mac
     * @return
     */
    public boolean isMac() {
        return this.mac;
    }

    /**
     * @see #onlyIfExists
     * @return
     */
    public boolean isOnlyIfExists() {
        return this.onlyIfExists;
    }

    /**
     * @see #windows
     * @return
     */
    public boolean isWindows() {
        return this.windows;
    }

    /**
     * @see #id
     * @return
     */
    public void setId(final String id) {
        this.id = id;
    }

    public void setLinux(final boolean linux) {
        this.linux = linux;
    }

    public void setMac(final boolean mac) {
        this.mac = mac;
    }

    public void setOnlyIfExists(final boolean onlyIfExists) {
        this.onlyIfExists = onlyIfExists;
    }

    public void setParentIDList(final ArrayList<String> parentIDList) {
        this.parentIDList = parentIDList;
    }

    public void setWindows(final boolean windows) {
        this.windows = windows;
    }

}
