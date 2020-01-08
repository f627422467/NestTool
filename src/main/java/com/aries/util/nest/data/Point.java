package com.aries.util.nest.data;

import java.math.BigDecimal;
import java.util.Objects;


/**
 * 点
 */
public class Point {

    private BigDecimal x;
    private BigDecimal y;

    private BigDecimal rx;
    private BigDecimal ry;
    private BigDecimal xAxisRotation;
    private BigDecimal largeArcFlag;
    private BigDecimal sweepFlag;

    public BigDecimal getX() {
        return x;
    }

    public void setX(BigDecimal x) {
        this.x = x;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setY(BigDecimal y) {
        this.y = y;
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

    public BigDecimal getxAxisRotation() {
        return xAxisRotation;
    }

    public void setxAxisRotation(BigDecimal xAxisRotation) {
        this.xAxisRotation = xAxisRotation;
    }

    public BigDecimal getLargeArcFlag() {
        return largeArcFlag;
    }

    public void setLargeArcFlag(BigDecimal largeArcFlag) {
        this.largeArcFlag = largeArcFlag;
    }

    public BigDecimal getSweepFlag() {
        return sweepFlag;
    }

    public void setSweepFlag(BigDecimal sweepFlag) {
        this.sweepFlag = sweepFlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x.compareTo(point.getX()) == 0 &&
                y.compareTo(point.getY()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
