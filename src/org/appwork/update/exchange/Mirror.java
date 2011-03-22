package org.appwork.update.exchange;

import org.appwork.storage.Storable;

public class Mirror implements Storable {
    private String url;
    private String hash;
    private int    priority = -1;
    private long   size;

    @SuppressWarnings("unused")
    private Mirror() {
        // required for Storable Interface
    }

    public Mirror(final String url, final String hash, final long length) {
        super();
        this.size = length;
        this.url = url;
        this.hash = hash;
    }

    /**
     * 256 SHA hash of the zip part. THIS is NOT the hash of the file.
     * Updatepackages are always build like this: <br>
     * |----64 bytes sha256 hash of the data frame ---|----data frame----| <br>
     * this hash, is the hash of the dataframe, and equals the 64 Byte header
     * 
     * @return
     */
    public String getHash() {
        return this.hash;
    }

    public int getPriority() {
        return this.priority;
    }

    public long getSize() {
        return this.size;
    }

    public String getUrl() {
        return this.url;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public void setUrl(final String url) {
        this.url = url;
    }
}
