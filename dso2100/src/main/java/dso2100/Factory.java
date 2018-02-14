package dso2100;

import dso.IOsciloscope;
import dso.IOsciloscopeFactory;

class Osciloscope2100ParallelFactory implements IOsciloscopeFactory {

    @Override
    public IOsciloscope createInstance() {
        return new Osciloscope2100Parallel();
    }
}
