package org.appwork.update.exchange;

import java.io.File;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.Storable;
import org.appwork.utils.Hash;

public class UpdateFile implements Comparable<UpdateFile>, Storable {

    /**
     * length of the resulting hash length of the hash method used in
     * {@link #hash(File)}
     */
    public static final int HASH_LENGTH = 64;

    public static String hash(final File lc) {
        // please note that you have to update #HASH_LENGTH if you change
        // hashmethod here
        return Hash.getSHA256(lc);
    }

    private UpdateFileOptions options = new UpdateFileOptions();

    private String            path;

    private String            hash;

    private boolean           newFile;

    @SuppressWarnings("unused")
    private UpdateFile() {
        // we need this constructor for JSONSerialisation
        // @see org.appwork.storage.Storable
    }

    public UpdateFile(final String path, final File f) {
        this.path = path;

        this.hash = UpdateFile.hash(f);
    }

    public UpdateFile(final String path, final String hash) {
        this.path = path;
        this.hash = hash;
    }

    @Override
    public int compareTo(final UpdateFile o) {
        return this.path.compareTo(o.path);
    }

    public String getHash() {
        return this.hash;
    }

    public UpdateFileOptions getOptions() {
        return this.options;
    }

    public String getPath() {
        return this.path;
    }

    public boolean isNewFile() {
        return this.newFile;
    }

    public boolean matches(final File srcFile) {
        return this.hash.equals(UpdateFile.hash(srcFile));
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public void setNewFile(final boolean b) {
        this.newFile = b;
    }

    public void setOptions(final UpdateFileOptions options) {
        this.options = options;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return JSonStorage.toString(this);
    }

}
