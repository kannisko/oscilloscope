package dso;

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
