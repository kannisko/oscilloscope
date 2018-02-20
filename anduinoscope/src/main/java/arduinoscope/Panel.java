package arduinoscope;

import javax.swing.*;

public class Panel {
    private JPanel panel;
    private JComboBox portComboBox;

    private Scope scope;

    public Panel() {

    }

    public JPanel getPanel() {
        return panel;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
        portComboBox.setModel(new DefaultComboBoxModel(scope.getEnumeratedPorts()));
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here


    }
}
