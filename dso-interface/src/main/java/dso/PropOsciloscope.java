package dso;

import java.util.Properties;

public abstract class PropOsciloscope implements IOsciloscope {
    private String ueerSettingsPrefix;
    private Properties userSettings;

    protected abstract void loadUserSettings();

    @Override
    public void setUserProperties(String ueerSettingsPrefix, Properties userSettings) {
        this.ueerSettingsPrefix = ueerSettingsPrefix;
        this.userSettings = userSettings;
        loadUserSettings();
    }
}
