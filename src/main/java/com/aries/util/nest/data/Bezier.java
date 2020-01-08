package com.aries.util.nest.data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 贝塞尔曲线
 */
public class Bezier {
    private BigDecimal x1;
    private BigDecimal y1;
    private BigDecimal x2;
    private BigDecimal y2;

    private BigDecimal x;
    private BigDecimal y;

    private List<Point> points;

    public BigDecimal getX1() {
        return x1;
    }

    public void setX1(BigDecimal x1) {
        this.x1 = x1;
    }

    public BigDecimal getY1() {
        return y1;
    }

    public void setY1(BigDecimal y1) {
        this.y1 = y1;
    }

    public BigDecimal getX2() {
        return x2;
    }

    public void setX2(BigDecimal x2) {
        this.x2 = x2;
    }

    public BigDecimal getY2() {
        return y2;
    }

    public void setY2(BigDecimal y2) {
        this.y2 = y2;
    }

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

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
