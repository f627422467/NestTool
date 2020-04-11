package com.aries.util.nest.data;

import com.aries.util.nest.util.UtilNumber;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * 多边形
 */
public class Polygon {


    private List<Point> points;

    private List<Line> lines;

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    /**
     * 根据当前的点集，重新计算多变形的边
     */
    public void reCompute() {
        lines = Lists.newArrayList();
        if (points.size() <= 1) {
            return;
        }
        for (int index = 1; index < points.size(); index++) {
            Point startPoint = points.get(index - 1);
            Point endPoint = points.get(index);
            Line line = new Line();
            line.setStartPoint(startPoint);
            line.setEndPoint(endPoint);
            lines.add(line);
        }

        Iterator<Line> lineIterator = lines.iterator();
        while (lineIterator.hasNext()){
            Line line = lineIterator.next();
            Point startPoint = line.getStartPoint();
            Point endPoint = line.getEndPoint();
            if(endPoint.equals(startPoint)){
                lineIterator.remove();
            }
        }
    }

    /**
     * 根据起始点获取线段
     * 
     * @return
     */
    public Line findLineByStartPoint(Point point) {
        for (Line line : this.lines) {
            Point startPoint = line.getStartPoint();
            if (UtilNumber.equals(point.getX(), startPoint.getX())
                && UtilNumber.equals(point.getY(), startPoint.getY())) {
                return line;
            }
        }
        return null;
    }

    /**
     * 根据终点获取线段
     * 
     * @param point
     * @return
     */
    public Line findLineByEndPoint(Point point) {
        for (Line line : this.lines) {
            Point endPoint = line.getEndPoint();
            if (UtilNumber.equals(point.getX(), endPoint.getX()) && UtilNumber.equals(point.getY(), endPoint.getY())) {
                return line;
            }
        }
        return null;
    }
}
