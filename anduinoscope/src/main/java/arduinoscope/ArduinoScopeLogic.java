package arduinoscope;

import dso.AquisitionFrame;
import dso.SlopeEdge;
import dso.XAxisSensivity;
import jssc.SerialPortException;
import nati.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;


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
    private Serial serialPort;// = new Serial();

    public ArduinoScopeLogic() {

    }

    HorizSensWithSampleRate selectedHoriz;
    int lastSettedSpeed = -1;

    private SlopeEdge slopeEdge = SlopeEdge.FALL;
    private int triggerLevel = 120;



    private static final int RETRY_CNT = 100;

    public void setSelectedHoriz(HorizSensWithSampleRate selectedHoriz) {
        this.selectedHoriz = selectedHoriz;
    }

    public static EnumeratedPort[] getEnumeratedPorts() {
        List<String> ports = Serial.enumeratePorts();

        EnumeratedPort result[] = new EnumeratedPort[ports.size() + 1];

        result[0] = new EnumeratedPort();
        int i = 1;
        for (String id : ports) {
            result[i++] = new EnumeratedPort(id);
        }
        return result;
    }

    public boolean connect(EnumeratedPort enumeratedPort) {
        try {
            disconnect();
            if (enumeratedPort.getPort() != null) {
                serialPort = new Serial(enumeratedPort.getPort());
//                serialPort.connect();
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


    public byte[] getData(BooleanSupplier cancel) throws IOException {
        serialPort.writeLine(CMD_ACTION_ACQUIRE_DATA);
        byte result[] = new byte[DATA_BUFFER_SIZE];
        serialPort.readBytes(result, cancel);
        return result;
    }

    private void cmdSetSpeed(int divisor) throws IOException, SerialPortException {
        serialPort.writeLine(CMD_SET_SPEED + " " + divisor);
        String res = serialPort.readLine();
        lastSettedSpeed = divisor;
        logger.warn("cmdSetSpeed:" + res);
    }

    private void cmdSetTriggerLevel() throws IOException, SerialPortException {
        serialPort.writeLine(CMD_SET_TRIGGER_VALUE + " " + this.triggerLevel);
        String res = serialPort.readLine();
        logger.warn("cmdSetTriggerLevel:" + res);

    }

    private void cmdSetTriggerSlope() throws IOException, SerialPortException {
        serialPort.writeLine(CMD_SET_TRIGGER_SLOPE + " " + this.slopeEdge.getCommand());
        String res = serialPort.readLine();
        logger.warn("cmdSetTriggerSlope:" + res);
    }




    public void setSlopeEdge(SlopeEdge edge) {
        this.slopeEdge = edge;
    }

    public void setTriggerLevel(int level) {
        if (level <= 0) {
            level = 0;
        } else if (level > 255) {
            level = 255;
        }
        this.triggerLevel = level;
    }
    public int getTriggerLevel(){
        return triggerLevel;
    }


    void updateParams() throws IOException, SerialPortException {
//      if (lastSettedSpeed != selectedHoriz.divisor) {
            cmdSetSpeed(selectedHoriz.divisor);
//      }
        cmdSetTriggerLevel();
        cmdSetTriggerSlope();

    }


    public void disconnect() throws IOException {
        logger.debug("disconnecting");
        serialPort.close();
    }


    public void acquireData(BooleanSupplier cancel, Consumer<AckDataResult> callback) {
        //AquisitionFrame
        try {
            updateParams();
            byte buffer[] = getData(cancel);
            logger.debug("acquireData OK");
            callback.accept(AckDataResult.OK(new AquisitionFrame(selectedHoriz.sampleRate, selectedHoriz.xAxisSensivity, buffer)));

        } catch (Exception ex) {
            logger.debug("acquireData ERR ", ex);
            callback.accept(AckDataResult.ERROR(ex));

        }
    }





    public static class EnumeratedPort {
        private String portIdentifier;

        public EnumeratedPort(String portIdentifier) {
            this.portIdentifier = portIdentifier;
        }

        public EnumeratedPort() {
        }

        String getPort() {
            return portIdentifier;
        }

        @Override
        public String toString() {
            if (portIdentifier != null) {
                return portIdentifier;
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

    public static void main(String args[]) throws Exception {
        EnumeratedPort ports[] = getEnumeratedPorts();
        if (ports.length <= 1) {
            return;
        }
        EnumeratedPort port = ports[1];
        ArduinoScopeLogic arduinoScopeLogic = new ArduinoScopeLogic();
        arduinoScopeLogic.connect(port);
        arduinoScopeLogic.connect(port);
        arduinoScopeLogic.connect(port);
        arduinoScopeLogic.connect(port);
        arduinoScopeLogic.connect(port);
        arduinoScopeLogic.connect(port);
    }

}
