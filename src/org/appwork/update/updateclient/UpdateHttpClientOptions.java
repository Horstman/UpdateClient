package org.appwork.update.updateclient;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.DefaultIntValue;

public interface UpdateHttpClientOptions extends ConfigInterface {
    @DefaultIntValue(60000)
    int getConnectTimeout();

    @DefaultIntValue(60000)
    int getReadTimeout();

}
