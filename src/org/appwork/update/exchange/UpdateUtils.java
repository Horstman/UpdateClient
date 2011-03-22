package org.appwork.update.exchange;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.StorageException;
import org.appwork.utils.Hash;
import org.appwork.utils.zip.ZipIOException;
import org.appwork.utils.zip.ZipUtils;

public class UpdateUtils {

    private static final int HASH_LENGTH = 64;

    public static UpdatePackage getUpdatePackage(final byte[] data) throws StorageException, UnsupportedEncodingException, ZipIOException, IOException {

        return JSonStorage.restoreFromString(ZipUtils.unzipString(data), UpdatePackage.class);
    }

    public static String unzipfromBytes(final byte[] data) throws ZipIOException, IOException {
        return ZipUtils.unzipString(data);
    }

    /**
     * 
     * 
     * @see org.appwork.update.exchange.Mirror#getHash()
     * @param file
     * @param hash
     * @throws IOException
     * @throws PackageException
     */
    public static void validateUpdatePackage(final File file, final String hash) throws IOException, PackageException {

        final String realHash = Hash.getSHA256(file);
        if (!realHash.equals(hash)) {
            file.delete();
            throw new PackageException(ServerError.DOWNLOADPACKAGE_VALIDATION_ERROR_INTERN);
        }
    }

    public static byte[] zipToBytes(final String fileList) throws ZipIOException, IOException {
        return ZipUtils.zipString(fileList);

    }
}
