package arduinoscope;

import dso.*;
import dso.guihelper.ComboWithProps;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.*;

public class GuiPanel implements IOsciloscope {
    private JPanel panel;
    private JComboBox portComboBox;
    public JComboBox horizontalSens;
    private JRadioButton triggerModeOff;
    private JRadioButton triggerModeAuto;
    private JRadioButton triggerModeNormal;
    private JRadioButton trModeSingle;
    private JRadioButton trModeSweep;
    private JRadioButton slopeRise;
    private JRadioButton slopeFall;
    private JSlider triggerLevel;
    private JButton startButton;

    private ArduinoScopeLogic arduinoScopeLogic;
    private IDsoGuiListener dsoGuiListener;
    private String userSettingPrefix;
    private Properties userSettings;

    ExecutorService service = Executors.newFixedThreadPool(10);
    public GuiPanel() {

        this.arduinoScopeLogic = new ArduinoScopeLogic();

    }

    @Override
    public AquisitionFrame acquireData() throws Exception {
        return null;
    }

    @Override
    public void disconnect() throws IOException {

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
                service.submit(connect);
                boolean isOk = false;
                try {
                    isOk = connect.get(1500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
                if (!isOk) {
                    JOptionPane.showMessageDialog(null, "My Goodness, this is so concise");
//                    portComboBox.setSelectedIndex(0);
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
