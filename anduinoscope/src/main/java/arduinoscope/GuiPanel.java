package arduinoscope;

import dso.*;
import dso.guihelper.ComboWithProps;
import dso.guihelper.GroupRadioWithProps;
import dso.guihelper.SliderWithProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.*;

public class GuiPanel implements IOsciloscope {
    protected static final Logger logger = LoggerFactory.getLogger(GuiPanel.class.getName());
    private JPanel panel;
    private JComboBox portComboBox;
    public JComboBox horizontalSens;
    private JRadioButton triggerModeOff;
    private JRadioButton triggerModeAuto;
    private JRadioButton triggerModeNormal;
    private JRadioButton trModeSingle;
    private JRadioButton trModeSweep;
    private GroupRadioWithProps<SlopeEdge> slopeEdge;
    private JRadioButton slopeRise;
    private JRadioButton slopeFall;
    private JSlider triggerLevel;
    private JButton startButton;
    private static final String BUTTON_START = "Start";

    private ArduinoScopeLogic arduinoScopeLogic;
    private IDsoGuiListener dsoGuiListener;
    private String userSettingPrefix;
    private Properties userSettings;
    private static final String BUTTON_STOP = "Stop";
    private boolean startFired = false;
    private FutureTask<Boolean> getDataJob = null;


    public GuiPanel() {

        this.arduinoScopeLogic = new ArduinoScopeLogic();

    }

    @Override
    public AquisitionFrame acquireData() throws Exception {
        return null;
    }

    @Override
    public void disconnect() throws IOException {
        arduinoScopeLogic.disconnect();

    }

    @Override
    public void setListener(IDsoGuiListener listener) {
        this.dsoGuiListener = listener;
        this.dsoGuiListener.setYAxis(YAxisSensivity.S_1V, YAxisPolarity.DC);

    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void setUserProperties(String userSettingPrefix, Properties userSettings) {
        this.userSettingPrefix = userSettingPrefix;
        this.userSettings = userSettings;

        portComboBox.setModel(new DefaultComboBoxModel(ArduinoScopeLogic.getEnumeratedPorts()));
        portComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Object o = portComboBox.getSelectedItem();
                RunnableFuture<Boolean> connect = new FutureTask<Boolean>(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return arduinoScopeLogic.connect((ArduinoScopeLogic.EnumeratedPort) o);
                    }
                });
                dsoGuiListener.getExecutorService().submit(connect);
                boolean isOk = false;
                try {
                    isOk = connect.get(1500, TimeUnit.MILLISECONDS);
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    logger.error(e.toString());
                    connect.cancel(true);
                }
                if (!isOk) {
                    JOptionPane.showMessageDialog(null, "My Goodness, this is so concise");
                    portComboBox.setSelectedIndex(0);
                }

            }
        });


        new ComboWithProps<>(horizontalSens
                , ArduinoScopeLogic.HorizSensWithSampleRate.values()
                , ArduinoScopeLogic.HorizSensWithSampleRate.h_20ms
                , this.userSettings
                , this.userSettingPrefix + ".ch.horizSens",
                o -> {
                    arduinoScopeLogic.setSelectedHoriz(o);
                    this.dsoGuiListener.setXAxis(o.xAxisSensivity);
                });

        slopeEdge = new GroupRadioWithProps<SlopeEdge>(
                new JRadioButton[]{slopeRise, slopeFall}
                , new SlopeEdge[]{SlopeEdge.RISE, SlopeEdge.FALL}
                , SlopeEdge.RISE
                , this.userSettings
                , this.userSettingPrefix + ".slope"
                , edge -> arduinoScopeLogic.setSlopeEdge(edge));

        new SliderWithProps(triggerLevel
                , 0, 255, 127
                , this.userSettings
                , this.userSettingPrefix + ".trigLvl"
                , value -> {
            arduinoScopeLogic.setTriggerLevel(value);
        });

        triggerModeOff.addActionListener(action -> {
            logger.debug("triggerModeOff action");
            stopGettingData();
            startButton.setEnabled(false);
            startButton.setText(BUTTON_START);
        });

        trModeSingle.addActionListener(actionEvent -> {
            logger.debug("trModeSingle action");
            stopGettingData();
            startButton.setEnabled(true);
            startButton.setText(BUTTON_START);
        });

        startButton.addActionListener(actionEvent -> {
            if (startFired) {
                logger.debug("startButton stopping action");
                stopGettingData();
                startFired = false;
                startButton.setText(BUTTON_START);

            } else {
                logger.debug("startButton starting action");
                startFired = true;
                startButton.setText(BUTTON_STOP);
                getDataJob = new FutureTask(() -> arduinoScopeLogic.acquireData(()->false,res -> callback(res)), true);
                dsoGuiListener.getExecutorService().submit(getDataJob);
            }
        });

//        private JRadioButton triggerModeAuto;
//        private JRadioButton triggerModeNormal;
//        private JRadioButton


    }

    private void callback(AckDataResult res) {
        logger.debug("callback");
        getDataJob = null;
        startFired = false;
        startButton.setText(BUTTON_START);
        if( res.isOk() ){
            this.dsoGuiListener.setData(res.getAquisitionFrame());
        }
    }

    private void stopGettingData() {
        if (getDataJob != null) {
            logger.debug("stopGettingData ");
            getDataJob.cancel(true);
        }
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here


    }

    public static class Factory implements IOsciloscopeFactory {

        public IOsciloscope createInstance() {
            return new GuiPanel();
        }

        @Override
        public String toString() {
            return "Arduinoscope";
        }
    }

}
