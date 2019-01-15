package dso.guihelper;

import javax.swing.*;
import java.util.Properties;
import java.util.function.Consumer;

public class SliderWithProps {
    public SliderWithProps(JSlider slider, int minValue, int maxValue, int defaultValue, Properties userSettings, String propName, Consumer<Integer> procedure) {
        slider.setMinimum(minValue);
        slider.setMaximum(maxValue);
        slider.addChangeListener(changeEvent -> {
            int value = slider.getValue();
            userSettings.setProperty(propName, Integer.toString(value));
            if (procedure != null) {
                procedure.accept(value);
            }

        });

        String value = userSettings.getProperty(propName);
        if (value != null && !value.isEmpty()) {
            try {
                defaultValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {

            }
        }
        slider.setValue(defaultValue);

    }
}
