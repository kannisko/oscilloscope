package nati;


import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
/**
 * Reading operations are semi-interruptible here. As long as nothing as been
 * read, it can be interrupted, but once something has been read, it continues
 * up to the line / buffer completion. This behavior is here to avoid to stop
 * reading a bunch of data sent by the Girino. It do it fast enough not to
 * bother us. The only delay we want to interrupt is when nothing is coming, per
 * instance when we wait for the trigger to happen. A crossover can still occur
 * - the trigger happening the same time the user cancel the operation - but it
 * is not likely to happen and the Girino doesn’t support a complex enough
 * protocol to prevent this kind of problem anyway. On the other hand, the
 * consequence is not fatal. We will read garbage the next time, display some
 * error to the user and move along.
 */
public class Serial implements Closeable {

    protected static final Logger logger = LoggerFactory.getLogger(Serial.class.getName());

    static {
        Native.setLibraryPath();
    }

    /**
     * The port we're normally going to use. Port detection could be forced by
     * setting a property: -Dgnu.io.rxtx.SerialPorts=portName
     */
    private static final Pattern[] ACCEPTABLE_PORT_NAMES = {
            //
            Pattern.compile("/dev/tty\\.usbserial-.+"), // Mac OS X
            Pattern.compile("/dev/tty\\.usbmodem.+"), // Mac OS X
            Pattern.compile("/dev/ttyACM\\d+"), // Raspberry Pi
            Pattern.compile("/dev/ttyUSB\\d+"), // Linux
            // Pattern.compile("/dev/rfcomm\\d+"), // Linux Bluetooth
            Pattern.compile("COM\\d+"), // Windows
    };

    /** Milliseconds to block while waiting for port open. */
    private static final int TIME_OUT = 2000;

    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 115200;

    /** Milliseconds to wait when no input is available. */
    private static final int READ_DELAY = 50;

    protected SerialPort serialPort;


    public Serial(String portId) throws Exception {
        connect(portId);
    }

    public void connect(String portId) throws Exception {
        logger.info("connecting {}", portId);
        serialPort = new SerialPort(portId);

//        serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
//
//        output = serialPort.getOutputStream();
//        input = serialPort.getInputStream();
//
//        serialPort.notifyOnDataAvailable(false);
    }

    public static List<String> enumeratePorts() {
        String names[] = SerialPortList.getPortNames();

        return Arrays.asList(names);
    }

    public String readLine() throws IOException, SerialPortException {
        if (serialPort == null) {
            return "";
        }
        StringBuilder line = new StringBuilder();
        int length = 0;
        try {
            while (true) {
                int c = serialPort.readBytes(1)[0];
                logger.debug("read int:{}", c);
                if (c >= 0) {
                    logger.debug("read byte:{}", (char) c);
                    if (c == '\n') {
                        break;
                    }

                    line.append((char) c);

                } else {
                    Thread.sleep(200);
                }

//                if ((input.available() > 0 || line.length() > 0) && (c = input.read()) >= 0) {
//                    line.append((char) c);
//                    ++length;
//                    boolean eol = length >= 2  && line.charAt(length - 1) == '\n';
//                    if (eol) {
//                        line.setLength(length - 1);
//                        break;
//                    }
//                } else {
//                    /*
//                     * Sleeping here allows us to be interrupted (the serial
//                     * input is not interruptible itself).
//                     */
//                    Thread.sleep(READ_DELAY);
//                }
            }
        } catch (InterruptedException e) {
            logger.debug("Read aborted");
            return "";
        }
        logger.debug("< ({})", line);
        return line.toString();
    }

    public int readBytes(byte[] buffer, BooleanSupplier cancel) throws IOException {
        int offset = 0;
//        try {
//            while (offset < buffer.length) {
//                if (input.available() > 0 || offset > 0) {
//                    int size = input.read(buffer, offset, buffer.length - offset);
//                    if (size < 0) {
//                        break;
//                    }
//                    offset += size;
//                } else {
//                    /*
//                     * Sleeping here allows us to be interrupted (the serial
//                     * input is not interruptible itself).
//                     */
//                    Thread.sleep(READ_DELAY);
//                }
//            }
//        } catch (InterruptedException e) {
//            logger.debug("Read aborted");
//            return -1;
//        }
        logger.debug("< {} byte(s)", offset);
        return offset;
    }

    public void writeLine(String line) throws IOException {
//        if (output == null) {
//            return;
//        }
//        for (int i = 0; i < line.length(); ++i) {
//            output.write(line.charAt(i));
//        }
//        output.write('\n');
//        output.flush();
        logger.debug("> ({})", line);
    }


    public void close() {
        if (serialPort != null) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
            serialPort = null;
        }
    }
}
