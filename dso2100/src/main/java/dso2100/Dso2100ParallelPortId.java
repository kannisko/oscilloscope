package dso2100;

import dso.DsoPortId;

/**
 * Created by pawel on 01.12.15.
 */
public class Dso2100ParallelPortId extends DsoPortId {
    private int address;

    public Dso2100ParallelPortId(int address) {
        this.address = address;
    }

    public int getAddress() {
        return address;
    }

    public String getName() {
        return "Dso2100Parallel:" + address;
    }
}
