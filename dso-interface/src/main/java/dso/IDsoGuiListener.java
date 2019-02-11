package dso;

import java.util.concurrent.ExecutorService;

public interface IDsoGuiListener {
    void setData(AquisitionFrame aquisitionFrame);

    void setYAxis(YAxisSensivity yAxisSensivity, YAxisPolarity yAxisPolarity);
    void setXAxis(XAxisSensivity xAxisSensivity);

    void setThreshold(int threshold);

    ExecutorService getExecutorService();
}
