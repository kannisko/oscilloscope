package dso.virtual;

import dso.*;
import dso.guihelper.ComboWithProps;
import dso.guihelper.GroupRadioWithProps;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

public class VirtualOscilloscope extends PropOsciloscope {
    static final int WIDTH = 300;
    static final byte VAL1 = 10;
    static final byte VAL2 = (byte) 240;
    private static final int INNER_VALUE_TAB_LEN_HALF = 5000;
    private static final int INNER_VALUE_TAB_LEN = INNER_VALUE_TAB_LEN_HALF * 2;
    IDsoGuiListener guiListener;
    int cnt = 0;
    byte val = VAL1;
    boolean isVal1 = true;
    float amplitude = 1.0f;
    int startX = 0;

    private JPanel panel;
    private JComboBox verticalSensChan1;
    private JPanel channel1;
    private JRadioButton sinRadioButton;
    private JRadioButton pulseRadioButton;
    private JRadioButton triangleRadioButton;
    GroupRadioWithProps<Shape> shape;
    private JTextField amplitudeText;
    private JSlider amplitudeSlider;
    private JSlider frequencySlider;
    private JComboBox horizSensChan1;

    private JComboBox samplingRateCombo;
    private ComboWithProps<SamplingRate> samplingRate;
    private double[] sinTable;
    private double[] triangleTable;
    private double[] squareTable;
    private int generatorFrequency = 100;

    public VirtualOscilloscope() {
        initConstData();

        amplitudeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                int val = amplitudeSlider.getValue();
                amplitude = (float) (0.01 * val);
            }
        });
        final float rangeFreq[] = {0.1f, 1.0f, 10.0f, 100.0f, 1000.0f, 1000.0f, 100000.0f};
        //Create the label table
        Hashtable labelTable = new Hashtable();
        labelTable.put(0, new JLabel("1"));
        labelTable.put(100, new JLabel("10"));
        labelTable.put(200, new JLabel("100"));
        labelTable.put(300, new JLabel("1k"));
        labelTable.put(400, new JLabel("10k"));
        labelTable.put(500, new JLabel("100k"));
        frequencySlider.setLabelTable(labelTable);
        frequencySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                int val = frequencySlider.getValue();
                int range = (val / 100);
                generatorFrequency = (int) (rangeFreq[range + 1] + (val % 100) * rangeFreq[range]);
                amplitudeText.setText(Integer.toString(generatorFrequency));
            }
        });



    }

    @Override
    protected void loadUserSettings() {

        this.shape = new GroupRadioWithProps<>(
                new JRadioButton[]{sinRadioButton,pulseRadioButton,triangleRadioButton}
                ,new Shape[]{Shape.SIN,Shape.PULSE,Shape.TRIANLE}
                ,Shape.SIN
                ,this.userSettings
                ,this.userSettingsPrefix+".gen.shape"
                ,null);

        new ComboWithProps<>(verticalSensChan1
                ,new YAxisSensivity[]{YAxisSensivity.S_1mV, YAxisSensivity.S_2mV, YAxisSensivity.S_5mV}
                ,YAxisSensivity.S_2mV
                ,this.userSettings
                ,this.userSettingsPrefix+".ch1.vertSens"
                ,o->guiListener.setYAxis(o));

        new ComboWithProps<>(horizSensChan1
                , XAxisSensivity.values()
                , XAxisSensivity.S_50ms
                ,this.userSettings
                ,this.userSettingsPrefix+".ch1.horizSens",
                o->this.guiListener.setXAxis(o));

        samplingRate = new ComboWithProps(samplingRateCombo
                , SamplingRate.values()
                , SamplingRate.sr10kS
                , this.userSettings
                , this.userSettingsPrefix + ".samplingRate"
        ,null);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("VirtualOscilloscope");
        frame.setContentPane(new VirtualOscilloscope().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static class Factory implements IOsciloscopeFactory {
        @Override
        public IOsciloscope createInstance() {
            return new VirtualOscilloscope();
        }

        @Override
        public String toString() {
            return "VirtualOscilloscope";
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public JPanel getPanel() {
        return panel;
    }

    public void disconnect() throws IOException {

    }

    @Override
    public void setListener(IDsoGuiListener listener) {
        this.guiListener = listener;
    }

    public AquisitionFrame acquireData() throws Exception {
        Thread.sleep(100);
        AquisitionFrame result = new AquisitionFrame();
        result.samplingFrequency = samplingRate.getValue().getSamplingRate();
        result.xAxisSenivity = (XAxisSensivity) horizSensChan1.getSelectedItem();

        result.data = new byte[2000];

        calcSin(result.data);
        return result;
    }

    private void calcSin(byte result[]) {

        int sum = 0;
        int generatorSampleRate = INNER_VALUE_TAB_LEN * generatorFrequency;
        for (int i = 0; i < result.length; i++) {

            double val = (getTabeValue(startX) * amplitude * 128 / 5.0) + 128;
            result[i] = (byte) val;
            sum += generatorSampleRate;
            while (sum >= samplingRate.getValue().getSamplingRate()) {
                startX++;
                if (startX >= INNER_VALUE_TAB_LEN) {
                    startX = 0;
                }
                sum -= samplingRate.getValue().getSamplingRate();
            }
        }
    }

    private double getTabeValue(int i) {
        switch (shape.getValue()) {
            case SIN:
                return sinTable[i];
            case PULSE:
                return squareTable[i];
            case TRIANLE:
                return triangleTable[i];
        }
        return 0.0;
    }

    private void initConstData() {
        sinTable = new double[INNER_VALUE_TAB_LEN];
        double step = 2.0 * Math.PI / INNER_VALUE_TAB_LEN;
        for (int i = 0; i < INNER_VALUE_TAB_LEN; i++) {
            sinTable[i] = Math.sin(i * step);
        }

        squareTable = new double[INNER_VALUE_TAB_LEN];
        for (int i = 0, j = INNER_VALUE_TAB_LEN_HALF; i < INNER_VALUE_TAB_LEN_HALF; i++, j++) {
            squareTable[i] = -1.0;
            squareTable[j] = 1.0;
        }

        triangleTable = new double[INNER_VALUE_TAB_LEN];
        int PEAK = (8 * INNER_VALUE_TAB_LEN) / 10;
        for (int i = 0; i < PEAK; i++) {
            triangleTable[i] = 1.0 * i / PEAK;
        }
        for (int i = PEAK, j = 0; i < INNER_VALUE_TAB_LEN; i++, j++) {
            triangleTable[i] = 1.0 - j * 1.0 / (INNER_VALUE_TAB_LEN - PEAK);
        }
    }

    private enum Shape {SIN, PULSE, TRIANLE}

}
