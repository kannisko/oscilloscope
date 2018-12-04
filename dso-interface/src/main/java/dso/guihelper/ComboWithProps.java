package dso.guihelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Created by Pawel.Piotrowski on 2018-12-04.
 */
public  class ComboWithProps<T> {
    private T value;
    public ComboWithProps(JComboBox combo,T model[], T defaultValue,Properties userSettings, String propName,Consumer<T> procedure){
        combo.setModel(new DefaultComboBoxModel(model));
        combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                value = (T)combo.getSelectedItem();
                if( procedure != null) {
                    procedure.accept(value);
                }
                userSettings.setProperty(propName,value.toString());
            }
        });
        String valName  = userSettings.getProperty(propName);
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).toString().equals(valName)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).toString().equals(defaultValue.toString())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
    public T getValue(){
        return value;
    }
}
