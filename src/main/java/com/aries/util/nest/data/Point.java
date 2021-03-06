package com.aries.util.nest.data;

import com.aries.util.nest.util.UtilNumber;

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
    private BigDecimal x_axis_rotation;
    private BigDecimal large_arc_flag;
    private BigDecimal sweep_flag;
    //是否向着图形所在的区域凹陷
    private boolean isArcInwards = false;

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

    public boolean getIsArcInwards() {
        return isArcInwards;
    }

    public void setIsArcInwards(boolean isArcInwards) {
        this.isArcInwards = isArcInwards;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        Point other = (Point)obj;
        if(!UtilNumber.equals(this.x, other.getX()) || !UtilNumber.equals(this.y, other.getY()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
