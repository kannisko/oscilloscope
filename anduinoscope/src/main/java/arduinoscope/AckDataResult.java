package arduinoscope;

import dso.AquisitionFrame;

public class AckDataResult {
    private boolean isOk;
    private AquisitionFrame aquisitionFrame;
    private Exception ex;

    private AckDataResult(boolean isOk, AquisitionFrame aquisitionFrame, Exception ex) {
        this.isOk = isOk;
        this.aquisitionFrame = aquisitionFrame;
        this.ex = ex;
    }

    public static AckDataResult OK(AquisitionFrame aquisitionFrame) {
        return new AckDataResult(true, aquisitionFrame, null);
    }

    public static AckDataResult ERROR(Exception ex) {
        return new AckDataResult(false, null, ex);
    }

    public boolean isOk() {
        return isOk;
    }

    public AquisitionFrame getAquisitionFrame() {
        return aquisitionFrame;
    }

    public Exception getEx() {
        return ex;
    }
}
