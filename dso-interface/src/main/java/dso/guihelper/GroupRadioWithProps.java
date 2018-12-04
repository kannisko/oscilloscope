package dso.guihelper;

/**
 * Created by Pawel.Piotrowski on 2018-12-04.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.function.Consumer;

public class GroupRadioWithProps<T> {
    private T value;

    private static class ButtonValue{
        JRadioButton button;
        Object buttonValue;

        public ButtonValue(JRadioButton button, Object buttonValue) {
            this.button = button;
            this.buttonValue = buttonValue;
        }
    };
    private ButtonValue buttons[];

    public GroupRadioWithProps(JRadioButton buttons[],
                               T values[],
                               T defaultValue,
                               Properties userSettings, String propName,Consumer<T> procedure){

        this.buttons = new ButtonValue[values.length];
        for( int i=0; i< values.length; i++ ){
            this.buttons[i] = new ButtonValue(buttons[i],values[i]);
        }


        for( ButtonValue buttonValue : this.buttons){
            buttonValue.button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                        value = (T)buttonValue.buttonValue;
                        userSettings.setProperty(propName,value.toString());
                        if(procedure != null){
                            procedure.accept(value);
                        }
                    }


            });
        }


        String valName  = userSettings.getProperty(propName);
        if( !selectValue(valName)) {
            if( !selectValue(defaultValue) ){
                selectValue(values[0]);
            }
        }
    }

    private boolean selectValue(T toSel){
        return selectValue(toSel.toString());
    }

    private boolean selectValue(String name){
        if( name ==  null || name.isEmpty()){
            return false;
        }
        for( ButtonValue buttonValue : buttons){
            if( buttonValue.buttonValue.toString().equals(name) ) {
                buttonValue.button.doClick();
                return true;
            }
        }
        return false;
    }

    public T getValue() {
        return value;
    }

}
