package dso.virtual;

import dso.IOsciloscope;
import dso.IOsciloscopeFactory;

public class Factory implements IOsciloscopeFactory {
    @Override
    public IOsciloscope createInstance() {
        return new VirtualOscilloscope(null);
    }
}
