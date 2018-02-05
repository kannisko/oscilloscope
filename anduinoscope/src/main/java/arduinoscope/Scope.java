package arduinoscope;

import gnu.io.CommPortIdentifier;
import nati.Serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Scope extends Serial{

    private static final int DATA_BUFFER_SIZE = 1280;
    private static final String ARDUSCOPE_VERSION = "#version 1.0";

    private static final String CMD_ACTION_RESET = "#ar";
    private static final String CMD_ACTION_ACQUIRE_DATA = "#aa";

    private static final String CMD_GET_VERSION = "#gv";
    private static final String CMD_SET_SPEED = "#ss";
    private static final String CMD_SET_TRIGGER_TYPE = "#stt";
    private static final String CMD_SET_TRIGGER_VALUE = "#stv";
    private static final String CMD_SET_TRIGGER_SLOPE = "#sts";



    private static final Logger logger = Logger.getLogger(Scope.class.getName());




    private static final int RETRY_CNT = 100;

    public boolean initDevice() throws IOException, InterruptedException {
        for( int i=0; i<RETRY_CNT; i++) {
            writeLine(CMD_ACTION_RESET);
            Thread.sleep(100);
            writeLine(CMD_GET_VERSION);
            String response = readLine();
            if( response.startsWith(ARDUSCOPE_VERSION)){
                return true;
            }
        }
        return false;
    }

    public byte[] getData() throws IOException {
        writeLine(CMD_ACTION_ACQUIRE_DATA);
        byte result[] = new byte[DATA_BUFFER_SIZE];
        readBytes(result);
        return result;
    }

    public static void main(String args[]) throws Exception {
        List<CommPortIdentifier> ports = Serial.enumeratePorts();
        if(ports.size()<=0){
            return;
        }
        CommPortIdentifier port = ports.get(0);
        Scope scope = new Scope();
        scope.connect(port);
        boolean init = scope.initDevice();
        logger.warning("init :"+( init? "OK":"NOT OK"));


        scope.close();
    }

}
