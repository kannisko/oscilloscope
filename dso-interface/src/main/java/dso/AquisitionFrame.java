package dso;

public class AquisitionFrame {
    //horiz
    public int samplingFrequency; //up to 2 GHz

    //per channel
    public XAxisSensivity xAxisSenivity;
    public byte data[];

    public AquisitionFrame() {
    }

    public AquisitionFrame(int samplingFrequency, XAxisSensivity xAxisSenivity, byte[] data) {
        this.samplingFrequency = samplingFrequency;
        this.xAxisSenivity = xAxisSenivity;
        this.data = data;
    }
}