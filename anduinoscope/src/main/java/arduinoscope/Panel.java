package arduinoscope;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Panel {
    private JPanel panel;
    private JComboBox portComboBox;
    public JComboBox horizontalSens;

    private Scope scope;

    public Panel() {

    }

    public JPanel getPanel() {
        return panel;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
        portComboBox.setModel(new DefaultComboBoxModel(scope.getEnumeratedPorts()));
        portComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Object o = portComboBox.getSelectedItem();
                scope.connect((Scope.EnumeratedPort) o);

            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here


    }
}
