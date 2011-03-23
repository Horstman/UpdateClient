package org.appwork.update.updateclient;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.appwork.controlling.State;
import org.appwork.storage.JSonStorage;
import org.appwork.update.exchange.Branch;
import org.appwork.update.exchange.BranchList;
import org.appwork.update.exchange.Mirror;
import org.appwork.update.exchange.MirrorList;
import org.appwork.update.exchange.PackageStatus;
import org.appwork.update.exchange.UpdateFile;
import org.appwork.update.exchange.UpdateFileOptions;
import org.appwork.update.exchange.UpdatePackage;
import org.appwork.update.exchange.UpdateUtils;
import org.appwork.update.updateclient.event.UpdaterEvent;
import org.appwork.update.updateclient.event.UpdaterEventSender;
import org.appwork.update.updateclient.http.HTTPIOException;
import org.appwork.update.updateclient.http.UpdateServerException;
import org.appwork.update.updateclient.stateapp.StateApp;
import org.appwork.update.updateclient.translation.T;
import org.appwork.utils.Application;
import org.appwork.utils.Files;
import org.appwork.utils.Hash;
import org.appwork.utils.IO;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.DownloadProgress;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.zip.ZipIOException;
import org.appwork.utils.zip.ZipIOReader;

public class Updater extends StateApp {

    private final String                                                   appID;
    private UpdateHttpClient                                               httpClient;

    private final UpdaterOptions                                           options;
    private UpdatePackage                                                  hashList;
    private BranchList                                                     branches;

    private final org.appwork.update.updateclient.event.UpdaterEventSender eventSender;
    private Branch                                                         branch;
    private UpdatePackage                                                  updates;
    private int                                                            version;
    private int                                                            updateMirror     = 0;
    private boolean                                                        canceled         = false;
    private Thread                                                         thread;
    private ArrayList<File>                                                installedFiles;
    private MirrorList                                                     mirrorURLs;
    private File                                                           file;
    private ArrayList<File>                                                extractedFiles;
    private ArrayList<File>                                                backups;
    protected int                                                          progress;
    public UpdaterState                                                    stateInit;

    public UpdaterState                                                    stateDone;
    public UpdaterState                                                    stateBranchUpdate;
    public UpdaterState                                                    stateCreatePackage;
    public UpdaterState                                                    stateDownloadHashList;
    /**
     * in this state, we compare the hashlist with the local files, and local
     * enviroment. After this State you can access {@link #getUpdates()},
     * {@link #getOptionals()} and {@link #getOptionalsDependencies()}
     */
    public UpdaterState                                                    stateFilter;
    public UpdaterState                                                    stateDownloadMirrorUrls;
    public UpdaterState                                                    stateDownloadData;
    public UpdaterState                                                    stateDownloadBranchList;
    public UpdaterState                                                    stateExtract;
    public UpdaterState                                                    stateWaitForUnlock;
    public UpdaterState                                                    stateInstall;
    public UpdaterState                                                    stateError;
    public ArrayList<InstalledFile>                                        filesToRemove;
    public InstallLogList                                                  installLog;
    private final String[]                                                 updateServers;
    public UpdatePackage                                                   optionals;
    public UpdatePackage                                                   optionalsDependencies;
    private static final String                                            BACKUP_EXTENSION = ".backup";

    public Updater(final UpdateHttpClient http, final UpdaterOptions options) {
        super();
        this.init();
        this.appID = options.getApp();
        this.thread = Thread.currentThread();
        this.httpClient = http;

        this.httpClient.putHeader("os", CrossSystem.getOSString());
        this.options = options;
        this.eventSender = new UpdaterEventSender();
        // randomize list
        final List<String> lst = Arrays.asList(this.options.getUpdServerList());
        Collections.shuffle(lst);
        this.updateServers = lst.toArray(new String[] {});

        final URL vFile = Application.getRessourceURL("version.dat");

        this.version = -1;
        if (vFile != null) {
            try {
                this.version = Integer.parseInt(IO.readURLToString(vFile));
            } catch (final Throwable e) {
                Log.exception(e);
            }
        }
    }

    /**
     * Removes all backup files and returns a list of all new installed files
     * 
     * @param backups
     * @return
     */
    private ArrayList<File> cleanUp(final ArrayList<File> backups) {
        final ArrayList<File> ret = new ArrayList<File>();
        int i = 0;
        for (final File f : backups) {
            i++;
            if (f.getName().endsWith(Updater.BACKUP_EXTENSION) && !f.delete()) {
                Log.L.warning("Could not clean up backup file " + f);
                ret.add(new File(f.getParentFile(), f.getName().substring(0, f.getName().length() - Updater.BACKUP_EXTENSION.length())));
            } else {
                ret.add(f);
            }

            this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_INSTALL, 70 + (int) (0.1 * Math.min(100, (i * 100 / backups.size())))));

        }
        return ret;

    }

    public String getAppID() {
        return this.appID;
    }

    public Branch getBranch() {
        return this.branch;
    }

    public org.appwork.update.updateclient.event.UpdaterEventSender getEventSender() {
        return this.eventSender;
    }

    public ArrayList<File> getFilesToInstall() {
        final ArrayList<File> ret = Files.getFiles(true, true, this.getTmpUpdateDirectory());
        if (ret.size() > 0) {
            ret.remove(0);
        }
        return ret;
    }

    public ArrayList<InstalledFile> getFilesToRemove() {
        return this.filesToRemove;
    }

    /**
     * Gets the hashlist. Downloads the list if required
     * 
     * @return
     * @throws HTTPIOException
     * @throws ParseException
     * @throws AppNotFoundException
     * @throws InterruptedException
     */
    public UpdatePackage getHashList() {
        return this.hashList;
    }

    public UpdateHttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * gets the current install and working directory
     * 
     * @return
     */
    public File getInstallDirectory() {
        String path = this.options.getWorkingDirectory();
        if (path == null) {
            path = Application.getResource("tmp").getParent();
        }
        File ret = new File(path);
        if (!ret.isAbsolute()) {
            ret = Application.getResource(path);
        }
        return ret;
    }

    public ArrayList<File> getInstalledFiles() {
        return this.installedFiles;
    }

    public File getInstallLogFile() {

        return new File(this.getInstallDirectory(), "installlog.json");
    }

    private String getJarName() {
        final String name = Updater.class.getName().replaceAll("\\.", "/") + ".class";
        final String url = Application.getRessourceURL(name).toString();

        final int index = url.indexOf(".jar!");
        if (index < 0) { return "NOT_JARED"; }
        try {
            return new File(new URL(url.substring(4, index + 4)).toURI()).getName();
        } catch (final MalformedURLException e) {
            Log.exception(Level.WARNING, e);

        } catch (final URISyntaxException e) {
            Log.exception(Level.WARNING, e);

        }
        return null;
    }

    public MirrorList getMirrorURLs() {
        return this.mirrorURLs;
    }

    /**
     * Gets the next Mirror out of the given list. This method respects the
     * Mirrors' priority value.
     * 
     * @param randomList
     *            .remove
     * @return
     */
    private Mirror getNextMirror(final ArrayList<Mirror> randomList) {
        int total = 0;
        for (final Mirror m : randomList) {
            total += Math.max(m.getPriority(), 0);
        }
        int random = (int) (Math.random() * total);
        Mirror ret;
        for (final Iterator<Mirror> it = randomList.iterator(); it.hasNext();) {
            ret = it.next();
            random -= Math.max(ret.getPriority(), 0);
            if (random <= 0) {
                it.remove();
                return ret;
            }
        }
        return randomList.remove(randomList.size() - 1);
    }

    private String getNextUpdateServer() {

        // remove / or \at the end

        String ret = this.updateServers[this.updateMirror];
        while (ret.endsWith("/") || ret.endsWith("\\")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret + "/" + this.version;
    }

    /**
     * Returns a list of all optional master files. (onlyIfExists Flag is set).
     * Optionals may require further files. check
     * {@link #getOptionalsDependencies()} <br>
     * this list is available after {@link #stateFilter}
     * 
     * @return
     */
    public UpdatePackage getOptionals() {
        return this.optionals;
    }

    /**
     * Returns a list of all dependencies of {@link #getOptionals()} <br>
     * this list is available after {@link #stateFilter}
     * 
     * @return
     */
    public UpdatePackage getOptionalsDependencies() {
        return this.optionalsDependencies;
    }

    public UpdaterOptions getOptions() {
        return this.options;
    }

    public int getProgress() {
        return this.progress;
    }

    public Thread getThread() {
        return this.thread;
    }

    private File getTmpUpdateDirectory() {
        return new File(this.getInstallDirectory(), "tmp/update/" + this.appID + "/" + this.getBranch().getName() + "/");
    }

    public UpdatePackage getUpdateList(final File root, final UpdatePackage pgk) {
        final UpdatePackage ret = new UpdatePackage();
        final int i = 0;
        for (final UpdateFile f : pgk) {
            final File lc = new File(root, f.getPath());

            if (!lc.exists()) {
                f.setNewFile(true);
                ret.add(f);
            } else {
                final String localHash = UpdateFile.hash(lc);
                if (!localHash.equalsIgnoreCase(f.getHash())) {
                    f.setNewFile(false);
                    ret.add(f);
                }
            }

        }
        return ret;
    }

    /**
     * Returns a list of all files which are outdated. This list does not cotain
     * optional files. <br>
     * this list is available after {@link #stateFilter}.
     * 
     * @see #getOptionals()
     * @see #getOptionalsDependencies()
     * 
     * @return
     */
    public UpdatePackage getUpdates() {
        return this.updates;

    }

    public int getVersion() {
        return this.version;
    }

    private void init() {
        /*
         * Declare all States as local classes
         */
        class InitState extends UpdaterState {
            public InitState() {
                super();
            }

            @Override
            public int getProgress() {
                return 0;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                return null;
            }

        }
        ;

        class DoneState extends UpdaterState {
            public DoneState() {
                super();
            }

            @Override
            public int getProgress() {
                return 100;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                return null;
            }

        }
        ;

        class BranchUpdateState extends UpdaterState {
            public BranchUpdateState() {
                super();
            }

            @Override
            public int getProgress() {
                return 4;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                Branch ret;
                if (Updater.this.options.getBranch() != null && Updater.this.options.getBranch().trim().length() > 0) {
                    ret = new Branch(Updater.this.options.getBranch());
                } else if (Updater.this.branches != null) {

                    ret = Updater.this.branches.get(Updater.this.branches.size() - 1);
                } else {
                    return Updater.this.stateDownloadBranchList;
                }
                if (ret != Updater.this.branch) {
                    Updater.this.branch = ret;
                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.BRANCH_UPDATED, Updater.this.branch.getName()));
                }

                return Updater.this.stateDownloadHashList;
            }

        }

        class CreatePackageState extends UpdaterState {
            public CreatePackageState() {
                super();
            }

            @Override
            public int getProgress() {
                return 25;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                // this loop will download the packagestatus in a givven
                // interval.
                // if the server returns a serverlist instead of the state, a
                // exception is thrown
                String status = null;

                try {
                    PackageStatus state = null;

                    while (state != PackageStatus.READY) {
                        if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                        Thread.sleep(Updater.this.options.getPackagePollInterval());

                        state = PackageStatus.valueOf(status = new String(Updater.this.httpClient.get(Updater.this.getNextUpdateServer() + "/serverlist/" + URLEncoder.encode(Updater.this.appID, "UTF-8") + "/" + URLEncoder.encode(Updater.this.getBranch().getName(), "UTF-8") + "/" + Updater.this.updates.createID())));
                        if (state == PackageStatus.FAILED) { throw new UpdateException("Package Creation Failed"); }
                        Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_SERVERLIST, -1));

                    }
                } catch (final IllegalArgumentException e) {
                    // finally, we get an exception as soon as status string
                    // does
                    // not
                    // contain a status but probably the serverlist

                    Updater.this.mirrorURLs = JSonStorage.restoreFromString(status, MirrorList.class);

                }

                return Updater.this.stateDownloadMirrorUrls;
            }

        }
        ;

        class DownloadHashlistState extends UpdaterState {
            public DownloadHashlistState() {
                super();
            }

            @Override
            public int getProgress() {
                return 7;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                final Branch branch = Updater.this.getBranch();
                try {

                    final File cacheFile = Application.getResource("tmp/update/" + URLEncoder.encode(Updater.this.appID, "UTF-8") + "_" + URLEncoder.encode(branch.getName(), "UTF-8") + ".dat");
                    cacheFile.getParentFile().mkdirs();
                    byte[] cacheData = null;
                    String cacheHash = "nocache";
                    try {
                        cacheData = IO.readFile(cacheFile);
                        cacheHash = Hash.getSHA256(cacheData);
                        cacheHash = "LL";
                    } catch (final Throwable e) {
                        // nothing
                    }
                    byte[] zip = Updater.this.httpClient.get(Updater.this.getNextUpdateServer() + "/filelist/" + URLEncoder.encode(Updater.this.appID, "UTF-8") + "/" + URLEncoder.encode(branch.getName(), "UTF-8") + "/" + cacheHash);
                    if (zip.length == 0) {
                        // cachedList is up2date
                        zip = cacheData;
                    } else {
                        cacheFile.delete();
                        IO.writeToFile(cacheFile, zip);
                    }
                    Updater.this.hashList = UpdateUtils.getUpdatePackage(zip);
                    System.out.println("Hashlist: " + Updater.this.hashList.size());
                } catch (final UpdateServerException e) {
                    switch (e.getType()) {
                        case UNKNOWN_BRANCH:
                            Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.BRANCH_RESET, Updater.this.options.getBranch()));
                            Updater.this.options.setBranch(null);
                            Updater.this.branch = null;
                            return Updater.this.stateBranchUpdate;
                    }
                    throw e;
                } catch (final HTTPIOException e) {
                    // RESET branch if branch could not be found

                    if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
                        // Server not found
                        if (Updater.this.nextUpdateServer()) {
                            return this.run();
                        } else {
                            throw e;
                        }

                    } else if (e.getResponseCode() >= 500) {
                        if (Updater.this.nextUpdateServer()) {
                            return this.run();
                        } else {
                            throw e;
                        }
                    } else {
                        throw e;
                    }
                }

                return Updater.this.stateFilter;
            }

        }
        ;

        class FilterState extends UpdaterState {

            private HashMap<String, UpdatePackage> idMap;

            public FilterState() {
                super();
            }

            private boolean filterEnviroment(final UpdateFile f, final File instDir, final File tmpDir) {
                if (Updater.this.options.isFullUpdate()) { return false; }
                final UpdateFileOptions opt = f.getOptions();
                if (opt == null) { return false; }
                if (Updater.this.options.isOsFilterEnabled()) {
                    // file is not for windows
                    if (!opt.isWindows() && CrossSystem.isWindows()) { return true; }
                    // file is not for linux
                    if (!opt.isLinux() && CrossSystem.isLinux()) { return true; }
                    // file is not for mac
                    if (!opt.isMac() && CrossSystem.isMac()) { return true; }
                }

                // update file only if it already exiosts
                final File installedFile = new File(instDir, f.getPath());
                final File tmpFile = new File(tmpDir, f.getPath());
                // ignore only if exists if this is a dependencies file
                if (opt.isOnlyIfExists() && opt.getParentIDList().size() == 0) {
                    Updater.this.optionals.add(f);
                    if (Updater.this.options.getUninstallList() != null && Arrays.asList(Updater.this.options.getUninstallList()).contains(opt.getId() == null ? f.getPath() : opt.getId())) { return true; }
                    if (!installedFile.exists() && !tmpFile.exists() && !Arrays.asList(Updater.this.options.getOptionalList()).contains(opt.getId() == null ? f.getPath() : opt.getId())) {

                    return true;

                    }
                }

                return false;
            }

            @Override
            public int getProgress() {
                return 10;
            }

            private boolean isFileRequired(final UpdateFile next) {
                if (next.getOptions() == null) { return true; }
                for (final String re : next.getOptions().getParentIDList()) {
                    final UpdatePackage list = this.idMap.get(re);
                    if (list != null) {
                        for (final UpdateFile uf : list) {
                            if (uf.getOptions().getParentIDList().size() > 0) {

                                if (this.isFileRequired(uf)) { return true; }
                                Log.L.fine("Update Optional " + next.getPath() + " Required by: " + re + "(" + uf.getPath() + ")");
                            } else {
                                // check if we have at least on parent
                                // file in
                                // list
                                Log.L.fine("Update Optional " + next.getPath() + " Required by: " + re + "(" + uf.getPath() + ")");
                                return true;
                            }
                        }
                    }
                }

                // no master file found
                // System.out.println("Rem " + next.getPath());
                return false;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                final UpdatePackage hashList = new UpdatePackage();
                Updater.this.optionals = new UpdatePackage();
                Updater.this.optionalsDependencies = new UpdatePackage();
                final File installDir = Updater.this.getInstallDirectory();
                final File tmpDir = Updater.this.getTmpUpdateDirectory();
                // first hashlistfilter

                for (final UpdateFile f : Updater.this.getHashList()) {
                    if (!this.filterEnviroment(f, installDir, tmpDir)) {
                        hashList.add(f);
                    }
                }

                // create map
                File lf;
                this.idMap = new HashMap<String, UpdatePackage>();
                HashMap<File, UpdateFile> map;
                String id;
                for (final UpdateFile f : hashList) {
                    id = f.getOptions() == null || f.getOptions().getId() == null ? f.getPath() : f.getOptions().getId();

                    UpdatePackage idList = this.idMap.get(id);
                    if (idList == null) {
                        idList = new UpdatePackage();
                        this.idMap.put(id, idList);
                    }
                    idList.add(f);

                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                }

                // filter options
                UpdateFile next;
                main: for (final Iterator<UpdateFile> it = hashList.iterator(); it.hasNext();) {
                    next = it.next();
                    if (next.getOptions() != null && next.getOptions().getParentIDList().size() > 0) {
                        Updater.this.optionalsDependencies.add(next);
                        if (this.isFileRequired(next)) {
                            continue main;
                        } else {
                            it.remove();
                        }
                    }

                }

                // get a list which does not contain files that have already
                // been
                // updated, but not moved

                // UpdatePackage list = getUpdateList(tmpDir, hashList);
                final UpdatePackage list = new UpdatePackage();
                int i = 0;
                for (final UpdateFile f : hashList) {
                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                    final File lc = new File(tmpDir, f.getPath());

                    if (!lc.exists()) {
                        f.setNewFile(true);
                        list.add(f);
                    } else {
                        final String localHash = UpdateFile.hash(lc);
                        if (!localHash.equalsIgnoreCase(f.getHash())) {
                            f.setNewFile(false);
                            list.add(f);
                        }
                    }
                    i++;
                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_FILTER, (int) (0.4 * Math.min(100, (i * 100 / hashList.size())))));

                    // Thread.sleep(10);

                }
                // remove all files that are already up2date
                Updater.this.installLog = new InstallLogList();
                final File logFile = Updater.this.getInstallLogFile();
                map = new HashMap<File, UpdateFile>();
                if (logFile.exists()) {
                    Updater.this.installLog = JSonStorage.restoreFrom(logFile, Updater.this.installLog);

                    for (final UpdateFile f : hashList) {
                        lf = new File(installDir, f.getPath());
                        map.put(lf, f);

                        if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                    }
                }

                i = 0;
                int total = list.size();
                for (final Iterator<UpdateFile> it = list.iterator(); it.hasNext();) {
                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                    next = it.next();
                    final File lc = new File(installDir, next.getPath());
                    if (!lc.exists()) {
                        next.setNewFile(true);

                    } else {
                        final String localHash = UpdateFile.hash(lc);
                        if (!localHash.equalsIgnoreCase(next.getHash())) {
                            next.setNewFile(false);

                        } else {
                            it.remove();

                            // check installlog. this file is in hashlist AND
                            // hash is ok. it should be in installog. if not,
                            // installog my be corrcupt
                            if (!map.containsKey(lc)) {
                                Updater.this.installLog.add(new InstalledFile(next.getPath(), lc.lastModified()));
                                Updater.this.installLog.setChanged(true);
                            }
                        }
                    }
                    i++;
                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_FILTER, 40 + (int) (0.4 * Math.min(100, (i * 100 / total)))));
                    // Thread.sleep(10);
                }
                if (Updater.this.installLog.isChanged()) {
                    Updater.this.installLog.setChanged(false);
                    JSonStorage.saveTo(Updater.this.getInstallLogFile(), Updater.this.installLog);
                }
                // restore installLog

                // find files to remove
                Updater.this.filesToRemove = new ArrayList<InstalledFile>();

                final File instRoot = Updater.this.getInstallDirectory();

                map = new HashMap<File, UpdateFile>();
                for (final UpdateFile f : hashList) {
                    lf = new File(installDir, f.getPath());
                    map.put(lf, f);

                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                }
                // fileMap contains localfile ->UpdateFile Mappings

                InstalledFile ifile;
                i = 0;
                total = Updater.this.installLog.size();
                for (final Iterator<InstalledFile> it = Updater.this.installLog.iterator(); it.hasNext();) {
                    i++;
                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                    ifile = it.next();
                    final File localFile = new File(instRoot, ifile.getRelPath());
                    if (!map.containsKey(localFile)) {
                        // file has been installed someday, but is not in
                        // filellist any
                        // more

                        if (localFile.exists()) {
                            Updater.this.filesToRemove.add(ifile);
                        }
                    }
                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_FILTER, 80 + (int) (0.2 * Math.min(100, (i * 100 / total)))));

                }

                Updater.this.updates = list;
                final ArrayList<File> waitingForInstall = Updater.this.getFilesToInstall();
                if (Updater.this.updates.size() == 0 && waitingForInstall.size() == 0 && Updater.this.filesToRemove.size() == 0) {
                    return Updater.this.stateDone;

                } else if (Updater.this.updates.size() == 0) {
                    return Updater.this.stateWaitForUnlock;

                } else {
                    return Updater.this.stateDownloadMirrorUrls;
                }

            }

        }
        ;

        class DownloadMirrorUrlsState extends UpdaterState {
            public DownloadMirrorUrlsState() {
                super();
            }

            @Override
            public int getProgress() {
                return 30;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.START_REPO_UPDATE));
                try {
                    final String updateID = Updater.this.updates.createID();
                    String status;
                    // returns either the mirrorlist or a packagestatus
                    status = new String(Updater.this.httpClient.get(Updater.this.getNextUpdateServer() + "/serverlist/" + URLEncoder.encode(Updater.this.appID, "UTF-8") + "/" + URLEncoder.encode(Updater.this.getBranch().getName(), "UTF-8") + "/" + updateID));

                    try {

                        // if this did not throw an exception, the package is
                        // not ready
                        // yet.
                        // handle the status
                        if (PackageStatus.valueOf(status) == PackageStatus.UNKNOWN) {
                            // package is unknown until now. request package
                            status = new String(Updater.this.httpClient.post(Updater.this.getNextUpdateServer() + "/request/" + URLEncoder.encode(Updater.this.appID, "UTF-8") + "/" + URLEncoder.encode(Updater.this.getBranch().getName(), "UTF-8") + "/" + updateID, Updater.this.updates.createPostData()));
                            final PackageStatus state = PackageStatus.valueOf(status);
                            if (state == PackageStatus.READY) {
                                return this.run();
                            } else if (state == PackageStatus.FAILED) { throw new UpdateException("Package Creation Failed"); }
                        }
                        return Updater.this.stateCreatePackage;
                    } catch (final IllegalArgumentException e) {
                        // finally, we get an exception as soon as status string
                        // does
                        // not
                        // contain a status but probably the serverlist

                        Updater.this.mirrorURLs = JSonStorage.restoreFromString(status, MirrorList.class);

                    }

                } finally {
                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.END_REPO_UPDATE));

                }
                return Updater.this.stateDownloadData;
            }

        }
        ;

        class DownloadDataState extends UpdaterState {
            public DownloadDataState() {
                super();
            }

            @Override
            public int getProgress() {
                return 30;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                final UpdatePackage pkg = Updater.this.updates;
                final MirrorList mirrors = (MirrorList) Updater.this.mirrorURLs.clone();
                final int length = mirrors.size();
                final ArrayList<Mirror> randomList = new ArrayList<Mirror>(mirrors.size());
                final Random rnd = new Random();
                for (int i = 0; i < length; i++) {
                    randomList.add(mirrors.remove(rnd.nextInt(mirrors.size())));
                }

                Updater.this.file = new File(Updater.this.getTmpUpdateDirectory(), "package_" + pkg.createID() + ".dat");
                Updater.this.file.getParentFile().mkdirs();

                Mirror mirror;
                while (randomList.size() > 0 && (mirror = Updater.this.getNextMirror(randomList)) != null) {
                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.START_DOWNLOAD_FILE, mirror));
                    try {
                        if (Updater.this.file.exists()) {
                            try {
                                UpdateUtils.validateUpdatePackage(Updater.this.file, mirror.getHash());
                                return Updater.this.stateExtract;
                            } catch (final Exception e) {
                                Log.exception(Level.WARNING, e);
                                Updater.this.file.delete();
                                // TODO: try to resume

                            }

                        }
                        // TODO
                        final DownloadProgress dp = new DownloadProgress() {

                            @Override
                            public void increaseLoaded(final long increase) {

                                super.increaseLoaded(increase);
                                Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_DOWNLOAD_FILE, (int) Math.min(100, (this.getLoaded() * 100 / this.getTotal()))));

                            }

                        };
                        dp.setTotal(mirror.getSize());
                        Updater.this.httpClient.download(Updater.this.file, mirror.getUrl(), dp);
                        try {
                            UpdateUtils.validateUpdatePackage(Updater.this.file, mirror.getHash());
                            return Updater.this.stateExtract;
                        } catch (final Exception e) {
                            Log.exception(Level.WARNING, e);
                            Updater.this.file.delete();
                        }
                    } finally {
                        Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.END_DOWNLOAD_FILE, mirror));

                    }

                }
                throw new UpdateException("Could not download file");

            }

        }
        class DownloadBranchlistState extends UpdaterState {
            public DownloadBranchlistState() {
                super();
            }

            @Override
            public int getProgress() {
                return 4;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                try {

                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                    String currentBranch = Updater.this.options.getCurrentBranch();
                    if (currentBranch == null) {
                        currentBranch = "nobranch";
                    }
                    Updater.this.branches = JSonStorage.restoreFromString(new String(Updater.this.httpClient.get(Updater.this.getNextUpdateServer() + "/branchlist/" + URLEncoder.encode(Updater.this.appID, "UTF-8") + "/" + URLEncoder.encode(currentBranch, "UTF-8"))), BranchList.class);

                } catch (final HTTPIOException e) {
                    if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
                        // Server not found
                        if (Updater.this.nextUpdateServer()) {
                            return this.run();
                        } else {
                            throw e;
                        }
                    } else if (e.getResponseCode() >= 500) {
                        if (Updater.this.nextUpdateServer()) {
                            return this.run();
                        } else {
                            throw e;
                        }
                    } else {
                        throw e;
                    }

                }

                return Updater.this.stateBranchUpdate;
            }

        }
        ;
        class ExtractState extends UpdaterState {
            public ExtractState() {
                super();
            }

            @Override
            public int getProgress() {
                return 70;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                ZipIOReader zip;
                boolean success = false;
                try {
                    zip = new ZipIOReader(Updater.this.file);
                    zip.setOverwrite(true);

                    final File tmpDir = Updater.this.getTmpUpdateDirectory();

                    Updater.this.extractedFiles = zip.extractTo(tmpDir);
                    success = true;

                    try {
                        // validate
                        @SuppressWarnings("unchecked")
                        final ArrayList<File> clone = (ArrayList<File>) Updater.this.extractedFiles.clone();

                        for (final UpdateFile f : Updater.this.updates) {
                            final File singleFile = new File(tmpDir, f.getPath());
                            if (!singleFile.exists()) {
                                throw new UpdateException("File missing: " + f.getPath());
                            } else if (!f.getHash().equals(UpdateFile.hash(singleFile))) {
                                // hash mismatch
                                throw new UpdateException("Hash mismatch after extracting: " + f.getPath());
                            } else {
                                // file ok
                                clone.remove(singleFile);
                            }
                        }
                        File f;
                        for (final Iterator<File> it = clone.iterator(); it.hasNext();) {
                            f = it.next();
                            if (f.isDirectory()) {
                                it.remove();
                            }
                        }

                        if (clone.size() > 0) { throw new UpdateException("Too many files in package: " + clone); }
                    } finally {
                        zip.close();
                        Updater.this.file.delete();
                    }

                } catch (final ZipIOException e) {
                    throw new UpdateException(e);

                } finally {
                    if (success) {
                        Updater.this.file.delete();
                        Updater.this.file.deleteOnExit();
                    }
                }

                return Updater.this.stateWaitForUnlock;
            }

        }
        class WaitForUnlockState extends UpdaterState {
            /**
             * Checks wether we can write to this file or no
             * 
             * @param file
             * @return
             */
            private boolean canWriteTo(final File file) {

                if (!file.exists()) { return true; }
                if (!file.canWrite()) { return false; }
                final File renameTest = new File(file.getAbsoluteFile() + ".test");
                renameTest.delete();
                final boolean suc = file.renameTo(renameTest);
                if (!suc) {
                    //
                    return false;
                    //
                }
                renameTest.renameTo(file);
                return true;
            }

            /**
             * Lists all jars in {@link #getInstallDirectory()} and checks if we
             * can write to them. returns true if all jars are free for
             * writing/renaming/removing
             * 
             * @return
             */
            private void canWriteToJars() throws LockedException {
                final String jar = Updater.this.getJarName();
                System.out.println("Jar: " + Updater.this.getJarName());
                final File[] jars = Updater.this.getInstallDirectory().listFiles(new FileFilter() {

                    @Override
                    public boolean accept(final File arg0) {
                        boolean ret;
                        if (Application.isJared(Updater.class)) {
                            ret = arg0.getName().endsWith(".jar") && (jar == null || !arg0.getName().equals(jar));
                        } else {
                            ret = arg0.getName().endsWith(".jar");
                        }

                        return ret;

                    }
                });

                for (final File f : jars) {
                    if (!this.canWriteTo(f)) { throw new LockedException(f);

                    }
                }

            }

            @Override
            public int getProgress() {
                return 75;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                boolean startEvent = false;
                try {
                    while (true) {
                        try {
                            this.canWriteToJars();
                            return Updater.this.stateInstall;
                        } catch (final LockedException e) {
                            if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                            Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.LOCKED, e.getFile()));
                            startEvent = true;
                            Thread.sleep(1000);
                        }
                    }
                } finally {
                    if (startEvent) {
                        Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.UNLOCKED));
                    }

                }

            }

        }
        ;

        class InstallState extends UpdaterState {

            public InstallState() {
                super();
            }

            @Override
            public int getProgress() {
                return 78;
            }

            private void install(final ArrayList<File> filesToInstall, final File installDir, final File update, final ArrayList<File> backups, final File updates) throws InterruptedException, InstallException {
                if (!updates.exists()) {
                    updates.mkdirs();

                }

                for (final File f : updates.listFiles()) {
                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                    if (f.isDirectory()) {
                        this.install(filesToInstall, installDir, update, backups, f);
                    } else {
                        final String relPath = Files.getRelativePath(update, f);

                        // final String hash = Hash.getMD5(f);
                        // use length to find virtualisation this is faster than
                        // hashes
                        final long length = f.length();
                        final File installDirFile = new File(installDir, relPath);
                        Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.START_INSTALL_FILE, installDirFile, relPath));
                        try {
                            if (!installDirFile.exists()) {
                                installDirFile.getParentFile().mkdirs();
                                if (!f.renameTo(installDirFile)) {
                                    //
                                    throw new InstallException(T._.could_not_install_file(installDirFile.getAbsolutePath()));
                                    //
                                }
                                // add to backup file. backup files without
                                // .backup
                                // extension will be deleted
                                Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_INSTALL, (int) (0.7 * Math.min(100, (backups.size() * 100 / filesToInstall.size())))));

                                backups.add(installDirFile);
                            } else {
                                final File backup = new File(installDirFile.getAbsolutePath() + Updater.BACKUP_EXTENSION);
                                if (backup.exists()) {
                                    Log.L.warning("Removed backupfile: " + backup);
                                }
                                if (!installDirFile.renameTo(backup)) { throw new InstallException(T._.could_not_overwrite(installDirFile.getAbsolutePath())); }
                                Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_INSTALL, (int) (0.7 * Math.min(100, (backups.size() * 100 / filesToInstall.size())))));

                                backups.add(backup);
                                if (!f.renameTo(installDirFile)) { throw new InstallException("Could not rename to " + installDirFile); }
                            }
                            // check if we can delete dir

                            final long newLength = installDirFile.length();
                            if (newLength == 0 && !installDirFile.exists()) {
                                throw new InstallException(T._.virtual_file_system_detected(installDirFile.toString()));
                            } else if (newLength != length) { throw new InstallException("Error While Moving. Length Mismatch"); }
                        } finally {
                            Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.END_INSTALL_FILE, installDirFile, relPath));
                        }

                    }

                    // cleans up directories.
                    File down = f.getParentFile();
                    String[] directSubFiles;
                    while (down != null && !down.equals(update.getParentFile()) && (directSubFiles = down.list()) != null && directSubFiles.length == 0 && Files.getFiles(false, true, down).size() == 0) {
                        down.delete();
                        down = down.getParentFile();
                    }
                }

            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            /**
             * write all new installed files to a config file. runs through this
             * config files. if we see, that we ever installed a file which is
             * now not in list any more.. remove it
             * 
             * @param installedFiles
             * @return
             * @throws InterruptedException
             * @throws ParseException
             * @throws HTTPIOException
             * @throws IOException
             * @throws AppNotFoundException
             */
            private ArrayList<File> removeOutdatedFiles(final ArrayList<File> newFiles) throws InterruptedException, HTTPIOException, ParseException, IOException {

                final File instRoot = Updater.this.getInstallDirectory();

                final ArrayList<File> ret = new ArrayList<File>();
                // fileMap contains localfile ->UpdateFile Mappings

                InstalledFile ifile;

                File localFile;
                for (int i = 0; i < Updater.this.filesToRemove.size(); i++) {
                    ifile = Updater.this.filesToRemove.get(i);
                    localFile = new File(instRoot, ifile.getRelPath());
                    if (localFile.exists()) {

                        if (localFile.delete()) {

                            // TODO improve speed
                            if (Files.getFiles(false, true, localFile.getParentFile()).size() == 0) {
                                Files.deleteRecursiv(localFile.getParentFile());
                            }
                            Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.DELETED_FILE, ifile));
                            Updater.this.installLog.remove(ifile);
                            Updater.this.installLog.setChanged(true);
                        }
                    } else {
                        Updater.this.installLog.remove(ifile);
                        Updater.this.installLog.setChanged(true);
                    }
                }
                final HashMap<File, UpdateFile> map = new HashMap<File, UpdateFile>();
                for (final UpdateFile f : Updater.this.getHashList()) {

                    map.put(new File(instRoot, f.getPath()), f);
                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                }
                if (newFiles != null && newFiles.size() > 0) {
                    UpdateFile uf;
                    for (final File f : newFiles) {
                        if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                        // comare to hashlist here. we only want to add files to
                        // installlog
                        // which have been downloaded.
                        uf = map.get(f);
                        if (uf != null) {
                            ifile = new InstalledFile(uf.getPath(), f.lastModified());
                            Updater.this.installLog.remove(ifile);
                            Updater.this.installLog.add(ifile);
                            Updater.this.installLog.setChanged(true);
                        }
                    }

                }
                if (Updater.this.installLog.isChanged()) {

                    JSonStorage.saveTo(Updater.this.getInstallLogFile(), Updater.this.installLog);
                    Updater.this.installLog.setChanged(false);
                }
                return ret;
            }

            private int revert(final ArrayList<File> backups) throws IOException {
                int error = 0;
                Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.START_REVERT));
                try {
                    int i = 0;
                    for (final File f : backups) {
                        final String path = f.getAbsolutePath();
                        Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_REVERT, (i++ * 100 / backups.size())));
                        if (path.endsWith(Updater.BACKUP_EXTENSION)) {
                            final String orgPath = path.substring(0, path.length() - Updater.BACKUP_EXTENSION.length());
                            final File orgFile = new File(orgPath);
                            orgFile.delete();
                            if (!f.renameTo(orgFile)) {
                                Log.L.severe("Could not revert failed update: " + f);
                                error++;
                            }

                        } else {
                            if (!f.delete()) {
                                Log.L.severe("Could not revert failed update: " + f);
                                error++;
                            } else {
                                if (Files.getFiles(false, true, f.getParentFile()).size() == 0) {
                                    // delete dir
                                    Files.deleteRecursiv(f.getParentFile());
                                }
                            }
                        }

                    }
                } finally {
                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_REVERT, 100));
                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.END_REVERT, error));
                }
                return error;

            }

            @Override
            public StateAction run() throws Exception {
                if (Updater.this.getOptions().isDebug()) {
                    Log.L.info("Files to Install:");
                    Log.L.info(JSonStorage.toString(Updater.this.getFilesToInstall()));
                    Log.L.info("Files to Download:");
                    Log.L.info(JSonStorage.toString(Updater.this.getUpdates()));

                    Log.L.info("Files to Remove:");
                    Log.L.info(JSonStorage.toString(Updater.this.getFilesToRemove()));

                }
                final ArrayList<File> filesToInstall = Updater.this.getFilesToInstall();
                // filter files.remove all files we cannot find in hashlist
                final HashMap<File, UpdateFile> map = new HashMap<File, UpdateFile>();
                for (final UpdateFile f : Updater.this.getHashList()) {

                    map.put(new File(Updater.this.getTmpUpdateDirectory(), f.getPath()), f);
                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                }
                File next;
                for (final Iterator<File> it = filesToInstall.iterator(); it.hasNext();) {
                    next = it.next();
                    if (next.isFile() && !map.containsKey(next)) {
                        Log.L.warning("removed " + next + " From tmp update dir");
                        if (next.delete()) {
                            if (Files.getFiles(false, true, next.getParentFile()).size() == 0) {
                                Files.deleteRecursiv(next.getParentFile());
                            }

                            Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.DELETED_FILE, new InstalledFile(Files.getRelativePath(Updater.this.getTmpUpdateDirectory(), next), next.lastModified())));
                            it.remove();
                        }
                    }

                }
                // System.out.println("HH");
                //
                final File tmp = Updater.this.getTmpUpdateDirectory();
                final ArrayList<File> ret = new ArrayList<File>();
                Updater.this.backups = new ArrayList<File>();
                try {
                    this.install(filesToInstall, Updater.this.getInstallDirectory(), tmp, Updater.this.backups, tmp);

                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                    final ArrayList<File> installedFiles = Updater.this.cleanUp(Updater.this.backups);
                    ret.addAll(installedFiles);
                    if (Updater.this.isInterrupted()) { throw new InterruptedException(); }
                    ret.addAll(this.removeOutdatedFiles(installedFiles));

                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.PROGRESS_INSTALL, 100));
                    // set current branch after installation
                    Updater.this.options.setCurrentBranch(Updater.this.getBranch().getName());
                } catch (final Throwable e) {
                    Log.exception(e);
                    Updater.this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.INSTALL_FAILED, e));
                    this.revert(Updater.this.backups);
                    if (e instanceof InterruptedException) { throw (InterruptedException) e; }
                    throw new UpdateException("Installation failed.", e);
                } finally {

                }

                Updater.this.installedFiles = ret;

                return Updater.this.stateDone;
            }

        }
        class ErrorState extends UpdaterState {
            @Override
            public int getProgress() {
                return 95;
            }

            @Override
            public void prepare() {
                Updater.this.progress = this.getProgress();
            }

            @Override
            public StateAction run() throws Exception {

                return null;
            }

        }
        ;
        // Define States
        this.stateInit = new InitState();
        this.stateDone = new DoneState();
        this.stateBranchUpdate = new BranchUpdateState();
        this.stateCreatePackage = new CreatePackageState();
        this.stateDownloadHashList = new DownloadHashlistState();
        this.stateFilter = new FilterState();
        this.stateDownloadMirrorUrls = new DownloadMirrorUrlsState();
        this.stateDownloadData = new DownloadDataState();
        this.stateDownloadBranchList = new DownloadBranchlistState();
        this.stateExtract = new ExtractState();
        this.stateWaitForUnlock = new WaitForUnlockState();
        this.stateInstall = new InstallState();
        this.stateError = new ErrorState();

        // straight forward
        State.link(this.stateInit, this.stateBranchUpdate, this.stateDownloadHashList, this.stateFilter, this.stateDownloadMirrorUrls, this.stateDownloadData, this.stateExtract, this.stateWaitForUnlock, this.stateInstall, this.stateDone);
        // exceptions
        // fetch branchlist loop
        this.stateDownloadBranchList.addChildren(this.stateBranchUpdate);
        this.stateBranchUpdate.addChildren(this.stateDownloadBranchList);
        // invalid branch.
        this.stateDownloadHashList.addChildren(this.stateBranchUpdate);
        // no updates,
        this.stateFilter.addChildren(this.stateDone, this.stateWaitForUnlock);
        // package must be created
        this.stateDownloadMirrorUrls.addChildren(this.stateCreatePackage);
        this.stateCreatePackage.addChildren(this.stateDownloadMirrorUrls);

        this.stateBranchUpdate.addChildren(this.stateDownloadBranchList, this.stateDownloadHashList);

        this.stateDownloadBranchList.addChildren(this.stateDownloadHashList, this.stateError);
        this.stateDownloadHashList.addChildren(this.stateFilter, this.stateError);

        // errors:
        this.stateDownloadBranchList.addChildren(this.stateError);
        this.stateDownloadHashList.addChildren(this.stateError);
        this.stateFilter.addChildren(this.stateError);
        this.stateDownloadData.addChildren(this.stateError);
        this.stateExtract.addChildren(this.stateError);
        this.stateWaitForUnlock.addChildren(this.stateError);
        this.stateInstall.addChildren(this.stateError);
        this.stateCreatePackage.addChildren(this.stateError);
        this.stateDownloadMirrorUrls.addChildren(this.stateError);
        this.stateError.addChildren(this.stateDone);
        super.init(this.stateInit, this.stateDone, this.stateError);
    }

    public boolean isInterrupted() {
        return this.canceled || Thread.currentThread().isInterrupted();
    }

    private boolean nextUpdateServer() {
        if (this.updateMirror < this.updateServers.length - 1) {
            this.updateMirror++;
            Log.L.warning("Use next Updateserver: " + this.updateMirror + " = " + this.getNextUpdateServer());
            return true;
        }
        return false;
    }

    @Override
    public void onStateEnter(final StateAction newState) {
        this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.STATE_ENTER, (UpdaterState) newState));

    }

    @Override
    public void onStateExit(final StateAction stateAction) {
        this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.STATE_EXIT, (UpdaterState) stateAction));

    }

    public void requestExit() {
        this.canceled = true;
        if (this.thread != null) {
            System.out.println("i " + this.thread);
            this.thread.interrupt();
            this.httpClient.interrupt();
            System.out.println(this.thread.isInterrupted());
        }
        this.eventSender.fireEvent(new UpdaterEvent(Updater.this, UpdaterEvent.Types.EXIT_REQUEST));
    }

    @Override
    public void reset() {
        super.reset();
        this.updates = null;
        this.backups = null;
        this.branches = null;
        this.file = null;
        this.mirrorURLs = null;

        this.branch = null;
        this.progress = 0;
        this.branches = null;
        this.hashList = null;
        this.updateMirror = 0;

    }

    public void setHttpClient(final UpdateHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setThread(final Thread th) {
        this.thread = th;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

}
