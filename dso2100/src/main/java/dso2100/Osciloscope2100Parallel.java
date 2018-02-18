package dso2100;

import dso.DsoPortId;
import dso.IDsoGuiListener;
import dso.IOsciloscope;
import dso.IOsciloscopeFactory;
import jnpout32.VirtualIOPort;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pawel on 01.12.15.
 */
public class Osciloscope2100Parallel extends Osciloscope2100 {
    VirtualIOPort parallelPort;                   // wrapper class for 'Jnpout32.dll'
    short baseAddr;
    short statusAddr;
    short controlAddr;
    private IDsoGuiListener guiListener;

    public static class Factory implements IOsciloscopeFactory {

        public IOsciloscope createInstance() {
            return new Osciloscope2100Parallel();
        }
    }

    public Osciloscope2100Parallel() {
        parallelPort = new VirtualIOPort();
    }

    public boolean setConnection( DsoPortId portId, Map parameters) throws Exception {
        baseAddr = (short) ((Dso2100ParallelPortId)portId).getAddress();
        statusAddr = (short) (baseAddr + 1);
        controlAddr = (short) (baseAddr + 2);
        parallelPort.Out32((short) (baseAddr + 0x402), (short) 0x20);
        while( !connect() ){

        }
        return true;
    }


    @Override
    public void setData(short data) {
        parallelPort.Out32(baseAddr, data);
    }

    @Override
    public short getData() {
        return parallelPort.Inp32(baseAddr);
    }

    @Override
    public void setStatus(short sts) {
        parallelPort.Out32(statusAddr, sts);
    }

    @Override
    public void setControl(short ctrl) {
        parallelPort.Out32(controlAddr, ctrl);
    }

    public static List<DsoPortId> enumeratePorts(){
        List list = new ArrayList();
        list.add(new Dso2100ParallelPortId(0xe800));
        return list;
    }

    @Override
    public void setListener(IDsoGuiListener listener) {
        this.guiListener = listener;

    }

    public JPanel getPanel() {
        return null;
    }
}
