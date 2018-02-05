package dso;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by Pawel.Piotrowski on 2015-11-27.
 */
public interface IDso {
    AquisitionFrame acquireData() throws Exception;
//    boolean setConnection(DsoPortId portId, Map parameters ) throws Exception;
    void disconnect() throws IOException;

    public static class AquisitionFrame {
        //horiz
        public int samplingFrequency;
        public IDsoGuiListener.XAxisSensivity xAxisSenivity;

        public byte data[];
    }
    JPanel getPanel();
}
