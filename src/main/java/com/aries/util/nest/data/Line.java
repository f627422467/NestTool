package com.aries.util.nest.data;

import java.math.BigDecimal;

public class Line {

    private Point startPoint;

    private Point endPoint;

    private boolean isArc = false;
    //弧线属性
    private BigDecimal rx;
    private BigDecimal ry;
    private BigDecimal x_axis_rotation;
    private BigDecimal large_arc_flag;
    private BigDecimal sweep_flag;
    //是否向着图形所在的区域凹陷
    private boolean isArcInwards = false;

    //一般式的参数
    private BigDecimal a;

    private BigDecimal b;

    private BigDecimal c;

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }

    public boolean getIsArc() {
        return isArc;
    }

    public void setIsArc(boolean isArc) {
        this.isArc = isArc;
    }

    public boolean getIsArcInwards() {
        return isArcInwards;
    }

    public void setIsArcInwards(boolean isArcInwards) {
        this.isArcInwards = isArcInwards;
    }

    public BigDecimal getRx() {
        return rx;
    }

    public void setRx(BigDecimal rx) {
        this.rx = rx;
    }

    public BigDecimal getRy() {
        return ry;
    }

    public void setRy(BigDecimal ry) {
        this.ry = ry;
    }

    public BigDecimal getX_axis_rotation() {
        return x_axis_rotation;
    }

    public void setX_axis_rotation(BigDecimal x_axis_rotation) {
        this.x_axis_rotation = x_axis_rotation;
    }

    public BigDecimal getLarge_arc_flag() {
        return large_arc_flag;
    }

    public void setLarge_arc_flag(BigDecimal large_arc_flag) {
        this.large_arc_flag = large_arc_flag;
    }

    public BigDecimal getSweep_flag() {
        return sweep_flag;
    }

    public void setSweep_flag(BigDecimal sweep_flag) {
        this.sweep_flag = sweep_flag;
    }

    public BigDecimal getA() {
        return a;
    }

    public void setA(BigDecimal a) {
        this.a = a;
    }

    public BigDecimal getB() {
        return b;
    }

    public void setB(BigDecimal b) {
        this.b = b;
    }

    public BigDecimal getC() {
        return c;
    }

    public void setC(BigDecimal c) {
        this.c = c;
    }

}
