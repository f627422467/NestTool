package com.aries.util.nest.data;

import org.junit.Test;


public class SegmentTest {

    @Test
    public void SegmentTest(){

        Segment s = new Segment(123.456576 , 432.432432);
        System.out.println(s.getX() +","+ s.getY());

    }

}
