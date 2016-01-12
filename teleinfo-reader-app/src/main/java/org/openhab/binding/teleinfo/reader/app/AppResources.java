package org.openhab.binding.teleinfo.reader.app;

import java.util.Locale;
import java.util.ResourceBundle;

public enum AppResources {

    APP_VERSION;

    static final ResourceBundle appResourcesBundle = ResourceBundle.getBundle("appResources", Locale.ENGLISH);

    public String getSimpleName() {
        return name();
    }

    public String getLocalized() {
        return appResourcesBundle.getString(name());

    }

    public String getLocalized(Locale locale) {
        return ResourceBundle.getBundle("appResource", locale).getString(name());
    }
}
