package dso;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Pawel.Piotrowski on 2015-11-27.
 */
public interface IDso {
//    boolean setConnection(DsoPortId portId, Map parameters ) throws Exception;
    void disconnect() throws IOException;
    byte[] acquireData() throws Exception;
    JPanel getPanel();
}
