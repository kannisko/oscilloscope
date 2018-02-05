package dso;

public interface IDsoGuiListener {
    public enum YAxisSensivity{

     S_1mV(1,"1mV/div"),S_2mV(2,"2mV/div"),S_5mV(5,"5mV/div"),
     S_10mV(10,"10mV/div"),S_20mV(20,"20mV/div"),S_50mV(50,"50mV/div"),
     S_100mV(100,"100mV/div"),S_200mV(200,"200mV/div"),S_500mV(500,"500mV/div"),
     S_1V(1000,"1V/div"),S_2V(2000,"2V/div"),S_5V(5000,"5vV/div");
     private int milivoltsPerDiv;
     private String name;
     YAxisSensivity(int milivoltsPerDiv,String name){
            this.milivoltsPerDiv = milivoltsPerDiv;
            this.name = name;
        }
        public int getMilivoltsPerDiv(){
         return milivoltsPerDiv;
        }
        public String toString(){
         return name;
        }
    }

    public enum XAxisSensivity{
        S_1us(1, "1us/div"), S_2us(2, "2us/div"), S_5us(5, "5us/div"),
        S_10us(10,"10us/div"),S_20us(20,"20us/div"),S_50us(50,"50us/div"),
        S_100us(100,"100us/div"),S_200us(200,"200us/div"),S_500us(500,"500us/div"),
        S_1ms(1000, "1ms/div"), S_2ms(2000, "2ms/div"), S_5ms(5000, "5ms/div"),
        S_10ms(10000, "10ms/div"), S_20ms(20000, "20ms/div"), S_50ms(50000, "50ms/div"),
        S_100ms(100000, "100ms/div"), S_200ms(200000, "200ms/div"), S_500ms(500000, "500ms/div"),
        S_1s(1000000, "1s/div"), S_2s(2000000, "2s/div"), S_5s(5000000, "5s/div");

        private String name;
        private int microsecondsPerDiv;
        private String unit;
        private int unitValue;

        XAxisSensivity(int microsecondsPerDiv, String name) {
            this.name = name;
            this.microsecondsPerDiv = microsecondsPerDiv;
            if (microsecondsPerDiv < 1000) {
                this.unit = "us";
                this.unitValue = microsecondsPerDiv;
            } else if (microsecondsPerDiv < 1000000) {
                this.unit = "ms";
                this.unitValue = microsecondsPerDiv / 1000;
            } else {
                this.unit = "s";
                this.unitValue = microsecondsPerDiv / 1000000;
            }
        }

        public int getUnitValue() {
            return unitValue;
        }

        public String getUnit() {
            return unit;
        }

        public int getMicrosecondsPerDiv() {
            return microsecondsPerDiv;
        }

        public String toString(){
            return name;
        }
    }
    void setYAxis(YAxisSensivity yAxisSensivity);
    void setXAxis(XAxisSensivity xAxisSensivity);
}
