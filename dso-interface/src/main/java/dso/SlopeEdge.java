package dso;

public enum SlopeEdge {
    RISE('r'), FALL('f');
    char command;
    private SlopeEdge(char command){
        this.command = command;
    }
    public char getCommand(){
        return command;
    }


}
