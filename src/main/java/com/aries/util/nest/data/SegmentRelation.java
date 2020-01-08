package com.aries.util.nest.data;

/**
 * @author aries
 */
public class SegmentRelation {
    public int type;
    public int A;
    public int B;

    public SegmentRelation(int type, int a, int b) {
        this.type = type;
        A = a;
        B = b;
    }

    public SegmentRelation() {
    }
}
