package arduinoscope;

import dso.*;
import dso.guihelper.ComboWithProps;
import gnu.io.CommPortIdentifier;
import nati.Serial;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;


public class Scope extends Serial implements IOsciloscope {


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
    HorizSensWithSampleRate selectedHoriz;
    int lastSettedSpeed = -1;
    private String userSettingPrefix;
    private Properties userSettings;



    private static final int RETRY_CNT = 100;

    private Panel panel;
    private IDsoGuiListener dsoGuiListener;

    private Scope() {
        this.panel = new Panel();
        this.panel.setScope(this);

    }

    public EnumeratedPort[] getEnumeratedPorts() {
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
                connect(enumeratedPort.getPort());
                return initDevice();
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

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

    public static void main(String args[]) throws Exception {
        List<CommPortIdentifier> ports = Serial.enumeratePorts();
        if (ports.size() <= 0) {
            return;
        }
        CommPortIdentifier port = ports.get(0);
        Scope scope = new Scope();
        scope.connect(port);
        boolean init = scope.initDevice();
        logger.warning("init :" + (init ? "OK" : "NOT OK"));
        scope.setSpeed(5);

        scope.close();
    }

    public byte[] getData() throws IOException {
        writeLine(CMD_ACTION_ACQUIRE_DATA);
        byte result[] = new byte[DATA_BUFFER_SIZE];
        readBytes(result);
        return result;
    }

    private void setSpeed(int divisor) throws IOException {
        writeLine(CMD_SET_SPEED + " " + divisor);
        String res = readLine();
        lastSettedSpeed = divisor;
        logger.warning("setSpeed:" + res);
    }

    void updateParams() throws IOException {
        if (lastSettedSpeed != selectedHoriz.divisor) {
            setSpeed(selectedHoriz.divisor);
        }

    }

    @Override
    public void disconnect() throws IOException {
        close();
    }

    @Override
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

    @Override
    public void setListener(IDsoGuiListener listener) {
        this.dsoGuiListener = listener;
        //  this.dsoGuiListener.setXAxis(XAxisSensivity.S_20ms);
        this.dsoGuiListener.setYAxis(YAxisSensivity.S_1V, YAxisPolarity.DC);

    }

    @Override
    public JPanel getPanel() {
        return panel.getPanel();
    }

    @Override
    public void setUserProperties(String userSettingPrefix, Properties userSettings) {
        this.userSettingPrefix = userSettingPrefix;
        this.userSettings = userSettings;

        new ComboWithProps<>(panel.horizontalSens
                , HorizSensWithSampleRate.values()
                , HorizSensWithSampleRate.h_20ms
                , this.userSettings
                , this.userSettingPrefix + ".ch.horizSens",
                o -> {
                    selectedHoriz = o;
                    this.dsoGuiListener.setXAxis(o.xAxisSensivity);
                });
    }

    public static class Factory implements IOsciloscopeFactory {

        public IOsciloscope createInstance() {
            return new Scope();
        }

        @Override
        public String toString() {
            return "Arduinoscope";
        }
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
    private enum HorizSensWithSampleRate {
        h_1ms(XAxisSensivity.S_1ms, 153000, 3),
        h_2ms(XAxisSensivity.S_2ms, 75500, 4),
        h_5ms(XAxisSensivity.S_5ms, 37300, 5),
        h_10ms(XAxisSensivity.S_10ms, 19390, 6),
        h_20ms(XAxisSensivity.S_20ms, 9740, 7),
        ;
        private XAxisSensivity xAxisSensivity;
        private int sampleRate;
        private int divisor;

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
