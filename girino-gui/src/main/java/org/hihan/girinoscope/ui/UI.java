package org.hihan.girinoscope.ui;

import dso.IDsoGuiListener;
import dso.dummy.VirtualOscilloscope;
import dso.IDso;
import gnu.io.CommPortIdentifier;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import nati.Serial;
import org.hihan.girinoscope.Native;
import org.hihan.girinoscope.ui.images.Icon;

@SuppressWarnings("serial")
public class UI extends JFrame implements IDsoGuiListener{

    private static final Logger logger = Logger.getLogger(UI.class.getName());

    public static void main(String[] args) throws Exception {

        Logger rootLogger = Logger.getLogger("org.hihan.girinoscope");
        rootLogger.setLevel(Level.WARNING);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        rootLogger.addHandler(handler);

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Native.setBestLookAndFeel();
                JFrame frame = new UI();
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    private IDso girino = new VirtualOscilloscope(this);

    private CommPortIdentifier portId;

   // private Map<Parameter, Integer> parameters = Girino.getDefaultParameters(new HashMap<Parameter, Integer>());

    private GraphPane graphPane;

    private Axis.Builder yAxisBuilder = new Axis.Builder();

    private StatusBar statusBar;

    private DataAcquisitionTask currentDataAcquisitionTask;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void setYAxis(YAxisSensivity yAxisSensivity) {
        int div = yAxisSensivity.getMilivoltsPerDiv();
        int max = 10*div;
        yAxisBuilder.setStartValue(-max).setEndValue(max).setIncrement(div);
        graphPane.setYCoordinateSystem(yAxisBuilder.build());
    }

    @Override
    public void setXAxis(XAxisSensivity xAxisSensivity) {

    }

    private class DataAcquisitionTask extends SwingWorker<Void, byte[]> {

        private CommPortIdentifier frozenPortId;

  //      private Map<Parameter, Integer> frozenParameters = new HashMap<Parameter, Integer>();

        public DataAcquisitionTask() {
            startAcquiringAction.setEnabled(false);
            stopAcquiringAction.setEnabled(true);
            exportLastFrameAction.setEnabled(true);
        }

        @Override
        protected Void doInBackground() throws Exception {
            while (!isCancelled()) {
                updateConnection();
                acquireData();
            }
            return null;
        }

        private void updateConnection() throws Exception {
            synchronized (UI.this) {
                frozenPortId = portId;
//                frozenParameters.putAll(parameters);
            }

            setStatus("blue", "Contacting Girino on %s...", "frozen");//frozenPortId.getName());

            Future<Void> connection = executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
 //                   girino.setConnection(frozenPortId, frozenParameters);
                    return null;
                }
            });
            try {
                connection.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new TimeoutException("No Girino detected on " + frozenPortId.getName());
            } catch (InterruptedException e) {
                connection.cancel(true);
                throw e;
            }
        }

        private void acquireData() throws Exception {
            setStatus("blue", "Acquiring data from %s...", "frozen");//frozenPortId.getName());
            Future<byte[]> acquisition = null;
            boolean terminated;
            do {
                boolean updateConnection = false;
                synchronized (UI.this) {
//                    parameters.put(Parameter.THRESHOLD, graphPane.getThreshold());
//                    parameters.put(Parameter.WAIT_DURATION, graphPane.getWaitDuration());
//                    updateConnection = !getChanges(frozenParameters).isEmpty() || frozenPortId != portId;
                }
                if (updateConnection) {
                    if (acquisition != null) {
                        acquisition.cancel(true);
                    }
                    terminated = true;
                } else {
                    try {
                        if (acquisition == null) {
                            acquisition = executor.submit(new Callable<byte[]>() {

                                @Override
                                public byte[] call() throws Exception {
                                    return girino.acquireData();
                                }
                            });
                        }
                        byte[] buffer = acquisition.get(1, TimeUnit.SECONDS);
                        if (buffer != null) {
                            publish(buffer);
                            acquisition = null;
                            terminated = false;
                        } else {
                            terminated = true;
                        }
                    } catch (TimeoutException e) {
                        // Just to wake up regularly.
                        terminated = false;
                    } catch (InterruptedException e) {
                        acquisition.cancel(true);
                        throw e;
                    }
                }
            } while (!terminated);
        }

        @Override
        protected void process(List<byte[]> buffer) {
            logger.log(Level.FINE, "{0} data buffer(s) to display.", buffer.size());
            graphPane.setData(buffer.get(buffer.size() - 1));
        }

        @Override
        protected void done() {
            startAcquiringAction.setEnabled(true);
            stopAcquiringAction.setEnabled(false);
            exportLastFrameAction.setEnabled(true);
            try {
                if (!isCancelled()) {
                    get();
                }
                setStatus("blue", "Done acquiring data from %s.", frozenPortId.getName());
            } catch (ExecutionException e) {
                setStatus("red", e.getCause().getMessage());
            } catch (Exception e) {
                setStatus("red", e.getMessage());
            }
        }
    }

    private final Action exportLastFrameAction = new AbstractAction("Export last frame", Icon.get("document-save.png")) {
        {
            putValue(Action.SHORT_DESCRIPTION, "Export the last time frame to CSV.");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            DateFormat format = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
            fileChooser.setSelectedFile(new File("frame-" + format.format(new Date()) + ".csv"));
            if (fileChooser.showSaveDialog(UI.this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                byte[] data = graphPane.getData();
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                    for (int i = 0; i < data.length; ++i) {
                        writer.write(String.format("%d;%d", i, data[i]));
                        writer.newLine();
                    }
                } catch (IOException e) {
                    setStatus("red", e.getMessage());
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            setStatus("red", e.getMessage());
                        }
                    }
                }
            }
        }
    };

    private final Action stopAcquiringAction = new AbstractAction("Stop acquiring", Icon.get("media-playback-stop.png")) {
        {
            putValue(Action.SHORT_DESCRIPTION, "Stop acquiring data from Girino.");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            currentDataAcquisitionTask.cancel(true);
        }
    };

    private final Action startAcquiringAction = new AbstractAction("Start acquiring", Icon.get("media-record.png")) {
        {
            putValue(Action.SHORT_DESCRIPTION, "Start acquiring data from Girino.");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            synchronized (UI.this) {
//                parameters.put(Parameter.THRESHOLD, graphPane.getThreshold());
//                parameters.put(Parameter.WAIT_DURATION, graphPane.getWaitDuration());
            }
            currentDataAcquisitionTask = new DataAcquisitionTask();
            currentDataAcquisitionTask.execute();
        }
    };

    private final Action aboutAction = new AbstractAction("About Girinoscope", Icon.get("help-about.png")) {

        @Override
        public void actionPerformed(ActionEvent event) {
            new AboutDialog(UI.this).setVisible(true);
        }
    };

    private final Action exitAction = new AbstractAction("Quit", Icon.get("application-exit.png")) {

        @Override
        public void actionPerformed(ActionEvent event) {
            dispose();
        }
    };

    public UI() {
        setTitle("Girinoscope");
        setIconImage(Icon.getImage("icon.png"));

        setLayout(new BorderLayout());

//        graphPane = new GraphPane(parameters.get(Parameter.THRESHOLD), parameters.get(Parameter.WAIT_DURATION));
        graphPane = new GraphPane(1,100);

        graphPane.setYCoordinateSystem(yAxisBuilder.build());
        float timeframe = 0.005F;
        String xFormat = timeframe > 0.005 ? "#,##0 ms" : "#,##0.0 ms";
        Axis xAxis = new Axis(0, timeframe * 1000, xFormat);
        graphPane.setXCoordinateSystem(xAxis);

        graphPane.setPreferredSize(new Dimension(800, 600));
        add(graphPane, BorderLayout.CENTER);

        setJMenuBar(createMenuBar());

        add(createToolBar(), BorderLayout.NORTH);

        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);


        add(getHardwarePanel(),BorderLayout.EAST);

        stopAcquiringAction.setEnabled(false);
        exportLastFrameAction.setEnabled(false);

        if (portId != null) {
            startAcquiringAction.setEnabled(true);
        } else {
            startAcquiringAction.setEnabled(false);
            setStatus("red", "No USB to serial adaptation port detected.");
        }
        startAcquiringAction.setEnabled(true);
    }

    JPanel getHardwarePanel(){
        return girino.getPanel();

    }
    @Override
    public void dispose() {
        try {
            if (currentDataAcquisitionTask != null) {
                currentDataAcquisitionTask.cancel(true);
            }
            executor.shutdownNow();
            try {
                executor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Serial line not responding.", e);
            }
            girino.disconnect();
        } catch (IOException e) {
            logger.log(Level.WARNING, "When disconnecting from Girino.", e);
        }
        super.dispose();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(exitAction);
        menuBar.add(fileMenu);

        JMenu girinoMenu = new JMenu("Girino");
        girinoMenu.add(createSerialMenu());
//        girinoMenu.add(createPrescalerMenu());
//        girinoMenu.add(createTriggerEventMenu());
//        girinoMenu.add(createVoltageReferenceMenu());
        menuBar.add(girinoMenu);

        JMenu displayMenu = new JMenu("Display");
        displayMenu.add(createChangeSignalInterpretationlAction());
        displayMenu.add(createDataStrokeWidthMenu());
        displayMenu.add(createThemeMenu());
        menuBar.add(displayMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(aboutAction);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JMenu createSerialMenu() {
        JMenu menu = new JMenu("Serial port");
        ButtonGroup group = new ButtonGroup();
        for (final CommPortIdentifier portId : Serial.enumeratePorts()) {
            Action setSerialPort = new AbstractAction(portId.getName()) {

                @Override
                public void actionPerformed(ActionEvent event) {
                    UI.this.portId = portId;
                }
            };
            AbstractButton button = new JCheckBoxMenuItem(setSerialPort);
            if (UI.this.portId == null) {
                button.doClick();
            }
            group.add(button);
            menu.add(button);
        }
        return menu;
    }

//    private JMenu createPrescalerMenu() {
//        JMenu menu = new JMenu("Acquisition rate / Time frame");
//        ButtonGroup group = new ButtonGroup();
//        for (final PrescalerInfo info : PrescalerInfo.values()) {
//            Action setPrescaler = new AbstractAction(info.description) {
//
//                @Override
//                public void actionPerformed(ActionEvent event) {
//                    synchronized (UI.this) {
//                        parameters.put(Parameter.PRESCALER, info.value);
//                    }
//                    String xFormat = info.timeframe > 0.005 ? "#,##0 ms" : "#,##0.0 ms";
//                    Axis xAxis = new Axis(0, info.timeframe * 1000, xFormat);
//                    graphPane.setXCoordinateSystem(xAxis);
//                }
//            };
//            AbstractButton button = new JCheckBoxMenuItem(setPrescaler);
//            if (info.reallyTooFast) {
//                button.setForeground(Color.RED.darker());
//            } else if (info.tooFast) {
//                button.setForeground(Color.ORANGE.darker());
//            }
//            if (info.value == parameters.get(Parameter.PRESCALER)) {
//                button.doClick();
//            }
//            group.add(button);
//            menu.add(button);
//        }
//        return menu;
//    }

//    private JMenu createTriggerEventMenu() {
//        JMenu menu = new JMenu("Trigger event mode");
//        ButtonGroup group = new ButtonGroup();
//        for (final TriggerEventMode mode : TriggerEventMode.values()) {
//            Action setPrescaler = new AbstractAction(mode.description) {
//
//                @Override
//                public void actionPerformed(ActionEvent event) {
//                    synchronized (UI.this) {
//                        parameters.put(Parameter.TRIGGER_EVENT, mode.value);
//                    }
//                }
//            };
//            AbstractButton button = new JCheckBoxMenuItem(setPrescaler);
//            if (mode.value == parameters.get(Parameter.TRIGGER_EVENT)) {
//                button.doClick();
//            }
//            group.add(button);
//            menu.add(button);
//        }
//        return menu;
//    }

//    private JMenu createVoltageReferenceMenu() {
//        JMenu menu = new JMenu("Voltage reference");
//        ButtonGroup group = new ButtonGroup();
//        for (final VoltageReference reference : VoltageReference.values()) {
//            Action setPrescaler = new AbstractAction(reference.description) {
//
//                @Override
//                public void actionPerformed(ActionEvent event) {
//                    synchronized (UI.this) {
//                        parameters.put(Parameter.VOLTAGE_REFERENCE, reference.value);
//                    }
//                }
//            };
//            AbstractButton button = new JCheckBoxMenuItem(setPrescaler);
//            if (reference.value == parameters.get(Parameter.VOLTAGE_REFERENCE)) {
//                button.doClick();
//            }
//            group.add(button);
//            menu.add(button);
//        }
//        return menu;
//    }

    private Action createChangeSignalInterpretationlAction() {
        Action setDisplayedSignalReferentia = new AbstractAction("Change signal interpretation") {

            @Override
            public void actionPerformed(ActionEvent event) {
                Axis.Builder builder = CustomAxisEditionDialog.edit(UI.this, yAxisBuilder);
                if (builder != null) {
                    yAxisBuilder = builder;
                    graphPane.setYCoordinateSystem(yAxisBuilder.build());
                }
            }
        };
        return setDisplayedSignalReferentia;
    }

    private JMenu createThemeMenu() {
        JMenu menu = new JMenu("Theme");
        ButtonGroup group = new ButtonGroup();
        for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            Action setLnF = new AbstractAction(info.getName()) {

                @Override
                public void actionPerformed(ActionEvent event) {
                    try {
                        UIManager.setLookAndFeel(info.getClassName());
                        SwingUtilities.updateComponentTreeUI(getRootPane());
                    } catch (Exception e) {
                        setStatus("red", "Failed to load {} LaF.", info.getName());
                    }
                }
            };
            AbstractButton button = new JCheckBoxMenuItem(setLnF);
            group.add(button);
            menu.add(button);
        }
        return menu;
    }

    private JMenu createDataStrokeWidthMenu() {
        JMenu menu = new JMenu("Data stroke width");
        ButtonGroup group = new ButtonGroup();
        for (final int width : new int[]{1, 2, 3}) {
            Action setStrokeWidth = new AbstractAction(width + " px") {

                @Override
                public void actionPerformed(ActionEvent event) {
                    graphPane.setDataStrokeWidth(width);
                }
            };
            AbstractButton button = new JCheckBoxMenuItem(setStrokeWidth);
            if (width == 1) {
                button.doClick();
            }
            group.add(button);
            menu.add(button);
        }
        return menu;
    }

    private JComponent createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        final Component start = toolBar.add(startAcquiringAction);
        final Component stop = toolBar.add(stopAcquiringAction);
        start.addPropertyChangeListener("enabled", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!start.isEnabled()) {
                    stop.requestFocusInWindow();
                }
            }
        });
        stop.addPropertyChangeListener("enabled", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!stop.isEnabled()) {
                    start.requestFocusInWindow();
                }
            }
        });
        toolBar.add(exportLastFrameAction);
        return toolBar;
    }

    private void setStatus(String color, String message, Object... arguments) {
        String formattedMessage = String.format(message != null ? message : "", arguments);
        final String htmlMessage = String.format("<html><font color=%s>%s</color></html>", color, formattedMessage);
        if (SwingUtilities.isEventDispatchThread()) {
            statusBar.setText(htmlMessage);
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    statusBar.setText(htmlMessage);
                }
            });
        }
    }

//    private Map<Parameter, Integer> getChanges(Map<Parameter, Integer> oldParameters) {
//        Map<Parameter, Integer> changes = new HashMap<Parameter, Integer>();
//        for (Map.Entry<Parameter, Integer> entry : parameters.entrySet()) {
//            Parameter parameter = entry.getKey();
//            Integer newValue = entry.getValue();
//            if (!same(newValue, oldParameters.get(parameter))) {
//                changes.put(parameter, newValue);
//            }
//        }
//        for (Map.Entry<Parameter, Integer> entry : oldParameters.entrySet()) {
//            Parameter parameter = entry.getKey();
//            if (!parameters.containsKey(parameter)) {
//                changes.put(parameter, null);
//            }
//        }
//        return changes;
//    }

    private static boolean same(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 == null || o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }
}
