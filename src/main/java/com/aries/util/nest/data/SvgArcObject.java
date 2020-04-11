package com.aries.util.nest.data;

import java.math.BigDecimal;

public class SvgArcObject {

    //圆心（焦点）
    private BigDecimal cx;
    private BigDecimal cy;

    //起始弧度
    private BigDecimal startAngle;
    //其实角度
    private BigDecimal startDegrees;

    private BigDecimal deltaAngle;

    //结束弧度
    private BigDecimal endAngle;
    //结束角度
    private BigDecimal endDegrees;

    public BigDecimal getCx() {
        return cx;
    }

    public void setCx(BigDecimal cx) {
        this.cx = cx;
    }

    public BigDecimal getCy() {
        return cy;
    }

    public void setCy(BigDecimal cy) {
        this.cy = cy;
    }

    public BigDecimal getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(BigDecimal startAngle) {
        this.startAngle = startAngle;
    }

    public BigDecimal getDeltaAngle() {
        return deltaAngle;
    }

    public void setDeltaAngle(BigDecimal deltaAngle) {
        this.deltaAngle = deltaAngle;
    }

    public BigDecimal getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(BigDecimal endAngle) {
        this.endAngle = endAngle;
    }

    public BigDecimal getStartDegrees() {
        return startDegrees;
    }

    public void setStartDegrees(BigDecimal startDegrees) {
        this.startDegrees = startDegrees;
    }

    public BigDecimal getEndDegrees() {
        return endDegrees;
    }

    public void setEndDegrees(BigDecimal endDegrees) {
        this.endDegrees = endDegrees;
    }
}
