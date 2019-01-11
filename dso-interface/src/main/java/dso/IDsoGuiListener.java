package dso;

import java.util.concurrent.ExecutorService;

public interface IDsoGuiListener {

    void setYAxis(YAxisSensivity yAxisSensivity, YAxisPolarity yAxisPolarity);
    void setXAxis(XAxisSensivity xAxisSensivity);

    ExecutorService getExecutorService();
}
