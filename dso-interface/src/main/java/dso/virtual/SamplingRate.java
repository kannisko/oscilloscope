package dso.virtual;

/**
 * Created by Pawel.Piotrowski on 2018-02-08.
 */
public enum SamplingRate {
    sr1kS(        1_000,   1,"kS\\s"),
    sr10kS(      10_000, 10,"kS\\s"),
    sr100kS(    100_000,100,"kS\\s"),
    sr1MS(    1_000_000,  1,"MS\\s"),
    sr10MS(  10_000_000, 10,"MS\\s"),
    sr100MS(100_000_000,100,"MS\\s");

    private int samplingRate;
    private String unit;
    private int unitSamplingRate;
    private String name;

    SamplingRate(int samplingRate,int unitSamplingRate,String unit){
        this.samplingRate = samplingRate;
        this.unitSamplingRate = unitSamplingRate;
        this.unit = unit;
        this.name = new StringBuffer()
                .append(unitSamplingRate)
                .append(unit)
                .toString();
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public String getUnit() {
        return unit;
    }

    public int getUnitSamplingRate() {
        return unitSamplingRate;
    }

    public String toString() {
        return name;
    }
}
