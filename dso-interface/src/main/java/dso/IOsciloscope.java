package dso;

import javax.swing.*;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Pawel.Piotrowski on 2015-11-27.
 */
public interface IOsciloscope {
    AquisitionFrame acquireData() throws Exception;
//    boolean setConnection(DsoPortId portId, Map parameters ) throws Exception;
    void disconnect() throws IOException;

    void setListener(IDsoGuiListener listener);
    JPanel getPanel();

    void setUserProperties(String userSettingPrefix, Properties userSettings);
}
