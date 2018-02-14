package dso2100;

import dso.AquisitionFrame;
import dso.IOsciloscope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Pawel.Piotrowski on 2015-11-27.
 */
public abstract class Osciloscope2100 implements IOsciloscope {
    Logger LOG = LoggerFactory.getLogger(Osciloscope2100.class.getName());
    private int offset = 0;

    public void disconnect() throws IOException {
    }


    public boolean connect() throws Exception {
        setControl((short) 0x20);
        setControl((short) 0x21);
        short resp = getData();
        setControl((short) 0x20);
        setupParameters(new Dso2100Parameters());
        sendAndReceiveStatus(0x04,0x14);
        return resp == 0x55;
    }

    private void sendAndReceiveStatus(int toSend, int status1, int status2) throws Exception {
        sendCommand((byte) toSend);
        short status;
        while ((status = receiveStatus()) == status1) {

        }
        if (status != status2) {
            throw new Exception("invalid response received " + Integer.toHexString(status) + " expected " + Integer.toHexString(status1) + " or " + Integer.toHexString(status2));
        }
    }

    private void sendAndReceiveStatus(int toSend, int wantedStatus) throws Exception {
        sendCommand((byte) toSend);
        short status = receiveStatus();
        if (status != wantedStatus) {
            throw new Exception("invalid response received " + Integer.toHexString(status) + " expected " + Integer.toHexString(wantedStatus));
        }
    }

    private void setupParameters(Dso2100Parameters self) throws Exception {
        while (true) {
            short status = receiveStatus();
            if (status == 0x0f) {
                break;
            }
            sendCommand((short) 0xFF);
            status = receiveStatus();

            if (status == 0x0f) {
                break;
            }
            sendCommand((short) 0x04);
        }

        sendAndReceiveStatus(self.ack_chn,0x0f);

        sendAndReceiveStatus(self.ack_chn,0x05);

        sendAndReceiveStatus(self.smp_rate,0x05);

        sendAndReceiveStatus(self.smp_rate,0x08);

        sendAndReceiveStatus(self.rd2_step,0x08);

        sendAndReceiveStatus(self.rd2_step,0x09);

        sendAndReceiveStatus(self.rd1_step,0x09);

        sendAndReceiveStatus(self.rd1_step,0x06);

        sendAndReceiveStatus(self.ch1_trig,0x06);

        sendAndReceiveStatus(self.ch1_trig,0x07);

        sendAndReceiveStatus(self.ch2_trig,0x07);

        sendAndReceiveStatus(self.ch2_trig,0x0a);

        sendAndReceiveStatus(self.rd2_shift,0x0a);

        sendAndReceiveStatus(self.rd2_shift,0x0b);

        sendAndReceiveStatus(self.trig_slope,0x0b);

        sendAndReceiveStatus(self.trig_slope,0x0c);

        sendAndReceiveStatus(self.ch1_v_offset,0x0c);

        sendAndReceiveStatus(self.ch1_v_offset,0x0d);

        sendAndReceiveStatus(self.ch2_v_offset,0x0d);

        sendAndReceiveStatus(self.ch2_v_offset,0x0e);

        sendAndReceiveStatus(self.trig_level,0x0e);

        sendAndReceiveStatus(self.trig_level,0x10);

        sendAndReceiveStatus(self.ch1_offset,0x10);

        sendAndReceiveStatus(self.ch1_offset,0x11);

        sendAndReceiveStatus(self.ch1_gain,0x11);

        sendAndReceiveStatus(self.ch1_gain,0x12);

        sendAndReceiveStatus(self.ch2_offset,0x12);

        sendAndReceiveStatus(self.ch2_offset,0x13);

        sendAndReceiveStatus(self.ch2_gain,0x13);

        sendAndReceiveStatus(self.ch2_gain,0x01);
    }

    public AquisitionFrame acquireData() throws Exception {



        byte buffer[] = new byte[2048];
        //  S04R0f..{R01} S02{R00}{R21} {S99{R21}} S99{R00}{R03} {L}
 //       sendAndReceiveStatus(0x04, 0x0f, 0x01);
   //     sendAndReceiveStatus(0x02, 0x00, 0x21);
     //   sendAndReceiveStatus(0x99, 0x21, 0x21);
       // sendAndReceiveStatus(0x99, 0x00, 0x03);


        for (int i = 0; i < 2048; i++) {
            buffer[i] = loadData();
        }

//        int pos = makeHi(0, offset++, buffer);
//        if (offset > 100) {
//            offset = 0;
//        }
//        pos = makeLo(pos, 50, buffer);
//        pos = makeHi(pos, 50, buffer);
//        pos = makeLo(pos, 100, buffer);
//        pos = makeHi(pos, 100, buffer);
//        pos = makeLo(pos, 50, buffer);
//        pos = makeHi(pos, 100, buffer);
//        pos = makeLo(pos, 100, buffer);
//        pos = makeHi(pos, 50, buffer);
        return new AquisitionFrame();
    }

    private int makeLo(int begin, int period, byte buffer[]) {
        return makeHiLo((byte) 30, begin, period, buffer);
    }

    private int makeHi(int begin, int period, byte buffer[]) {
        return makeHiLo((byte) 200, begin, period, buffer);
    }

    int makeHiLo(byte val, int begin, int period, byte buffer[]) {
        period += begin;
        while (begin < period) {
            buffer[begin++] = val;
        }
        return period;
    }

    public abstract void setData(short data);

    public abstract short getData();

    public abstract void setStatus(short sts);

    public abstract void setControl(short ctrl);


    private void ndelay(int nanos) {
        long endtime = System.nanoTime() + nanos;
        while (endtime >= System.nanoTime()) {

        }
    }

    protected short receiveStatus() {
        setControl((short) 0x22);
        setControl((short) 0x22);

        setControl((short) 0x23);
        setControl((short) 0x23);
        ndelay(1000);
        short status = getData();
        LOG.info(String.format("R%02X ", status));
        setControl((short) 0x22);
        setControl((short) 0x22);
        setControl((short) 0x24);
        return status;
    }

    protected void sendCommand(short command) {
        setControl((short) 0x06);
        ndelay(1000);
        setData(command);
        LOG.info(String.format("\nS%02X ", command));
        ndelay(1000);
        setControl((short) 0x06);
        setControl((short) 0x07);
        setControl((short) 0x06);
        setControl((short) 0x06);
        setControl((short) 0x26);
        setControl((short) 0x24);
    }

    byte loadData() {
        setControl((short) 0x2a);
        setControl((short) 0x2b);
        ndelay(1000);
        byte data = (byte) getData();
        setControl((short) 0x2a);
        return data;
    }

    public static class Dso2100Parameters {
        byte auto_trig=1;

        byte ack_chn=0x02;// #Acq CH1 (01,02 = CH1,CH2)
        byte smp_rate=0x02;// #01,02,03,04,05 .. = 100,50,25,20,10 .. MSa/s)
        byte rd2_step=0x02;//; #readout2 step + 1 (02,03 ..,0b)
        byte rd1_step=0x02;// #readout1 step + 1 (02,03 ..,0b)
        byte ch1_trig=0x3b;// # set CH1 AC, 5 V/Div, Trig CH1
        //#(00,04,08 = DC,GND,AC)
          //      #(01,02,20,21,22,30,31,32,33 = 10m,20m,50m,0.1,0.2,0.5,1,2,5 V/Div)
            //    #(00,40,80 = Trig CH1,EXT,CH2)
        byte ch2_trig=0x3b;// #set CH2 AC, 5 V/Div, Trig AC
        //#(00,40,80 = Trig AC,TV-V,TV-H)
        byte rd2_shift=0x01;// #readout2 shift (01,02,03,04,05 .. = 0,5,10,15,20 ..)
        byte trig_slope=0x01;// #set Trig Slope pos (01,02 = neg,pos)
        byte ch1_v_offset=0x66;
        byte ch2_v_offset=0x5d;
        byte trig_level=(byte)0x80;
        byte ch1_offset=(byte)0xaa;
        byte ch2_offset=(byte)0x89;
        byte ch1_gain=(byte)0x7c;
        byte ch2_gain=(byte)0xba;
    }

}
