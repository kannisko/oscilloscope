package arduinoscope;

import dso.*;
import dso.guihelper.ComboWithProps;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

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


    public void setArduinoScopeLogic(ArduinoScopeLogic arduinoScopeLogic) {
        this.arduinoScopeLogic = arduinoScopeLogic;
        portComboBox.setModel(new DefaultComboBoxModel(arduinoScopeLogic.getEnumeratedPorts()));
        portComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Object o = portComboBox.getSelectedItem();
                try {
                    arduinoScopeLogic.connect((ArduinoScopeLogic.EnumeratedPort) o);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
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
