package arduinoscope;

import dso.AquisitionFrame;
import dso.XAxisSensivity;
import gnu.io.CommPortIdentifier;
import nati.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


public class ArduinoScopeLogic {
    private static final Logger logger = LoggerFactory.getLogger(ArduinoScopeLogic.class.getName());

    private static final int DATA_BUFFER_SIZE = 1280;
    private static final String ARDUSCOPE_VERSION = "#version 1.0";

    private static final String CMD_ACTION_RESET = "#ar";
    private static final String CMD_ACTION_ACQUIRE_DATA = "#aa";

    private static final String CMD_GET_VERSION = "#gv";
    private static final String CMD_SET_SPEED = "#ss";
    private static final String CMD_SET_TRIGGER_TYPE = "#stt";
    private static final String CMD_SET_TRIGGER_VALUE = "#stv";
    private static final String CMD_SET_TRIGGER_SLOPE = "#sts";
    private Serial serialPort = new Serial();

    public ArduinoScopeLogic() {

    }

    HorizSensWithSampleRate selectedHoriz;
    int lastSettedSpeed = -1;



    private static final int RETRY_CNT = 100;

    public void setSelectedHoriz(HorizSensWithSampleRate selectedHoriz) {
        this.selectedHoriz = selectedHoriz;
    }

    public static EnumeratedPort[] getEnumeratedPorts() {
        List<CommPortIdentifier> ports = Serial.enumeratePorts();

        EnumeratedPort result[] = new EnumeratedPort[ports.size() + 1];

        result[0] = new EnumeratedPort();
        int i = 1;
        for (CommPortIdentifier id : ports) {
            result[i++] = new EnumeratedPort(id);
        }
        return result;
    }

    public boolean connect(EnumeratedPort enumeratedPort) {
        try {
            disconnect();
            if (enumeratedPort.getPort() != null) {
                serialPort = new Serial();
                serialPort.connect(enumeratedPort.getPort());
                return initDevice();
            }
            logger.info("connected");
            return true;

        } catch (Exception e) {
            logger.error(e.toString());
        }
        return false;

    }

    public boolean initDevice() {
        logger.info("initializing device");
        try {
            for (int i = 0; i < RETRY_CNT; i++) {
                serialPort.writeLine(CMD_ACTION_RESET);
                Thread.sleep(100);
                serialPort.writeLine(CMD_GET_VERSION);
                String response = serialPort.readLine();
                if (response.startsWith(ARDUSCOPE_VERSION)) {
                    logger.info("initialized");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return false;
    }

//    public static void main(String args[]) throws Exception {
//        List<CommPortIdentifier> ports = Serial.enumeratePorts();
//        if (ports.size() <= 0) {
//            return;
//        }
//        CommPortIdentifier port = ports.get(0);
//        ArduinoScopeLogic arduinoScopeLogic = new ArduinoScopeLogic();
//        arduinoScopeLogic.connect(port);
//        boolean init = arduinoScopeLogic.initDevice();
//        logger.warning("init :" + (init ? "OK" : "NOT OK"));
//        arduinoScopeLogic.setSpeed(5);
//
//        arduinoScopeLogic.close();
//    }

    public byte[] getData() throws IOException {
        serialPort.writeLine(CMD_ACTION_ACQUIRE_DATA);
        byte result[] = new byte[DATA_BUFFER_SIZE];
        serialPort.readBytes(result);
        return result;
    }

    private void setSpeed(int divisor) throws IOException {
        serialPort.writeLine(CMD_SET_SPEED + " " + divisor);
        String res = serialPort.readLine();
        lastSettedSpeed = divisor;
        logger.warn("setSpeed:" + res);
    }

    void updateParams() throws IOException {
        if (lastSettedSpeed != selectedHoriz.divisor) {
            setSpeed(selectedHoriz.divisor);
        }

    }


    public void disconnect() throws IOException {
        logger.debug("disconnecting");
        serialPort.close();
    }


    public AquisitionFrame acquireData() throws Exception {
        updateParams();
        byte buffer[] = getData();
        if (buffer == null) {
            return null;
        }
        AquisitionFrame frame = new AquisitionFrame();
        frame.samplingFrequency = selectedHoriz.sampleRate;
        frame.xAxisSenivity = selectedHoriz.xAxisSensivity;
        frame.data = buffer;
        return frame;
    }





    public static class EnumeratedPort {
        private CommPortIdentifier portIdentifier;

        public EnumeratedPort(CommPortIdentifier portIdentifier) {
            this.portIdentifier = portIdentifier;
        }

        public EnumeratedPort() {
        }

        CommPortIdentifier getPort() {
            return portIdentifier;
        }

        @Override
        public String toString() {
            if (portIdentifier != null) {
                return portIdentifier.getName();
            }
            return "<no port selected>";
        }
    }

    /*
    128	9.74 ± 0.04
    64	19.39 ± 0.06
    32	37.3 ± 0.6
    16	75.5 ± 0.3
    8	153 ± 2

     */
    public enum HorizSensWithSampleRate {
        h_1ms(XAxisSensivity.S_1ms, 153000, 3),
        h_2ms(XAxisSensivity.S_2ms, 75500, 4),
        h_5ms(XAxisSensivity.S_5ms, 37300, 5),
        h_10ms(XAxisSensivity.S_10ms, 19390, 6),
        h_20ms(XAxisSensivity.S_20ms, 9740, 7),
        ;
        public XAxisSensivity xAxisSensivity;
        public int sampleRate;
        public int divisor;

        HorizSensWithSampleRate(XAxisSensivity xAxisSensivity, int sampleRate, int divisor) {
            this.xAxisSensivity = xAxisSensivity;
            this.sampleRate = sampleRate;
            this.divisor = divisor;
        }

        public String toString() {
            return xAxisSensivity.toString();
        }
    }

}