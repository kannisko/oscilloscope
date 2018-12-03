package dso;

import java.util.Properties;

public abstract class PropOsciloscope implements IOsciloscope {
    protected String userSettingsPrefix;
    protected Properties userSettings;

    protected abstract void loadUserSettings();

    @Override
    public void setUserProperties(String userSettingPrefix, Properties userSettings) {
        this.userSettingsPrefix = userSettingPrefix;
        this.userSettings = userSettings;
        loadUserSettings();
    }
}
