package org.appwork.update.updateclient;

import java.io.File;

import org.appwork.update.updateclient.http.ClientUpdateRequiredException;
import org.appwork.update.updateclient.http.HTTPIOException;
import org.appwork.update.updateclient.http.UpdateServerException;
import org.appwork.utils.net.DownloadProgress;

public interface UpdateHttpClient {

    void download(File file, String url, DownloadProgress progress) throws HTTPIOException, InterruptedException, ClientUpdateRequiredException, UpdateServerException;

    public byte[] get(String url) throws HTTPIOException, InterruptedException, ClientUpdateRequiredException, UpdateServerException;

    UpdateHttpClientOptions getOptions();

    void interrupt();

    public byte[] post(String url, String data) throws HTTPIOException, InterruptedException, ClientUpdateRequiredException, UpdateServerException;

    void putHeader(String string, String osString);

}
