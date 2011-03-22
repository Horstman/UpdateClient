package org.appwork.update.updateclient.translation;

import java.io.IOException;
import java.net.URISyntaxException;

import org.appwork.txtresource.TranslationFactory;
import org.appwork.txtresource.TranslationUtils;

public class T {

    public static final UpdateTranslation _ = TranslationFactory.create(UpdateTranslation.class);

    public static void main(final String[] args) throws URISyntaxException, IOException {
        TranslationUtils.createFiles(false, UpdateTranslation.class);
    }
}
