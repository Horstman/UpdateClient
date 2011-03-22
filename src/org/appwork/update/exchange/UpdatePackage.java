package org.appwork.update.exchange;

import java.util.ArrayList;
import java.util.Collections;

import org.appwork.utils.Hash;

public class UpdatePackage extends ArrayList<UpdateFile> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UpdatePackage() {

    }

    public String createPostData() {
        Collections.sort(this);
        final StringBuilder sb = new StringBuilder();
        for (final UpdateFile d : this) {
            if (sb.length() > 0) {
                sb.append("\r\n");
            }
            sb.append(d.getHash() + "|" + d.getPath());
        }
        return sb.toString();

    }

    public String createID() {

        return Hash.getSHA256(createPostData());

    }
}
