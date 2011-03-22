package org.appwork.update.updateclient;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.DefaultStringArrayValue;
import org.appwork.storage.config.annotations.DefaultStringValue;
import org.appwork.storage.config.annotations.Description;

public interface UpdaterOptions extends ConfigInterface {
    @Description("AppID")
    String getApp();

    @Description("Predefine a branch for this updater")
    String getBranch();

    String getCurrentBranch();

    // boolean getNoUpdates();

    String[] getOptionalList();

    @Description("Interval the client uses to poll for a finished package")
    @DefaultLongValue(5000)
    long getPackagePollInterval();

    @DefaultStringValue("")
    String getRestartCommand();

    String[] getUninstallList();

    @Description("List of Pathes to Updateserver root. e.g. http://192.168.2.250/jcgi/")
    @DefaultStringArrayValue({ "http://upd0.appwork.org/jcgi/" })
    String[] getUpdServerList();

    @Description("Installdirectory of the application")
    @DefaultStringValue("")
    String getWorkingDirectory();

    @DefaultBooleanValue(false)
    boolean isDebug();

    // void setRestart(String b);
    @DefaultBooleanValue(false)
    boolean isFullUpdate();

    @DefaultBooleanValue(false)
    boolean isGuiless();

    @DefaultBooleanValue(true)
    boolean isOsFilterEnabled();

    // boolean setNoUpdates();

    void setApp(String app);

    void setBranch(String branch);

    void setCurrentBranch(String name);

    void setDebug(boolean b);

    void setFullUpdate(boolean b);

    void setGuiless(boolean b);

    void setOptionalList(String[] list);

    void setOsFilterEnabled(boolean b);

    void setRestartCommand(String b);

    void setUninstallList(String[] split);

    void setWorkingDirectory(String dir);
}
