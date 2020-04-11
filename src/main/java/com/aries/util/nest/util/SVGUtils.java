package com.aries.util.nest.util;

import com.aries.util.nest.data.Line;
import com.aries.util.nest.data.Point;
import com.aries.util.nest.data.Polygon;
import com.aries.util.nest.data.SvgArcObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SVGUtils {
    /**
     *
     * 根据多边形相交的部分，计算出新的多边形
     *
     * @param mainPolygon
     * @param polygons
     * @param inPolygons
     * @return
     */
    public static void cutAllGraph(Polygon mainPolygon, List<Polygon> polygons, List<Polygon> inMainPolygons) {
        boolean hasNext = false;
        if (UtilMisc.isNotEmpty(inMainPolygons)) {
            Iterator<Polygon> polygonIterator = inMainPolygons.iterator();
            while (polygonIterator.hasNext()) {
                Polygon polygon = polygonIterator.next();
                boolean allInPolygon = true;
                boolean allOutPolygon = true;
                List<Point> inMainGraphPoints = Lists.newArrayList();
                for (Point point : polygon.getPoints()) {
                    boolean isInPolygon = isInPolygon(point, mainPolygon);
                    if (isInPolygon) {
                        allOutPolygon = false;
                        inMainGraphPoints.add(point);
                    } else {
                        allInPolygon = false;
                    }
                }
                if (allInPolygon) {
                    continue;
                }
                boolean reserve = false;
                if(allOutPolygon){
                    //如果全部点都不在主视图上，则需要判断，主视图的点是否在切除图上
                    allInPolygon = true;
                    allOutPolygon = true;
                    for (Point point : mainPolygon.getPoints()) {
                        boolean isInPolygon = isInPolygon(point, polygon);
                        if (isInPolygon) {
                            allOutPolygon = false;
                            inMainGraphPoints.add(point);
                        } else {
                            allInPolygon = false;
                        }
                    }
                    //此时大小两个矩形没有任何交点
                    if (allOutPolygon) {
                        continue;
                    }
                    if (allInPolygon) {
                        continue;
                    }
                    //处理
                    reserve = true;
                }
                if (UtilMisc.isEmpty(inMainGraphPoints)) {
                    //TODO 具体报错，后续处理 (如果报错的可能太多零件报错了，会导致拆解大量失败)
                    //throw new BasicBusinessException("两个图形没有相交");
                    continue;
                }
                //去除重复,并保持顺序
                Iterator<Point> pointIterator = inMainGraphPoints.iterator();
                Set<Point> newPoints = Sets.newHashSet();
                List<Point> newPointLists = Lists.newArrayList();
                while (pointIterator.hasNext()){
                    Point point = pointIterator.next();
                    if(newPoints.add(point)){
                        newPointLists.add(point);
                    }
                }
                inMainGraphPoints.clear();
                inMainGraphPoints.addAll(newPointLists);
                if(!reserve){
                    cutGtaph(polygon, mainPolygon, inMainGraphPoints);
                }else{
                    cutGtaphReserve(polygon, mainPolygon, inMainGraphPoints);
                }
                polygonIterator.remove();
                hasNext = true;
                break;
            }
            if (hasNext) {
                cutAllGraph(mainPolygon, polygons, inMainPolygons);
            }
        }

        Iterator<Polygon> polygonIterator = polygons.iterator();
        while (polygonIterator.hasNext()) {
            Polygon polygon = polygonIterator.next();
            boolean allInPolygon = true;
            boolean allOutPolygon = true;
            List<Point> inMainGraphPoints = Lists.newArrayList();
            for (Point point : polygon.getPoints()) {
                boolean isInPolygon = isInPolygon(point, mainPolygon);
                if (isInPolygon) {
                    allOutPolygon = false;
                    inMainGraphPoints.add(point);
                } else {
                    allInPolygon = false;
                }
            }
            if (allInPolygon) {
                inMainPolygons.add(polygon);
                polygonIterator.remove();
                continue;
            }
            boolean reserve = false;
            if(allOutPolygon){
                //如果全部点都不在主视图上，则需要判断，主视图的点是否在切除图上
                allInPolygon = true;
                allOutPolygon = true;
                for (Point point : mainPolygon.getPoints()) {
                    boolean isInPolygon = isInPolygon(point, polygon);
                    if (isInPolygon) {
                        allOutPolygon = false;
                        inMainGraphPoints.add(point);
                    } else {
                        allInPolygon = false;
                    }
                }
                //此时大小两个矩形没有任何交点
                if (allOutPolygon) {
                    inMainPolygons.add(polygon);
                    polygonIterator.remove();
                    continue;
                }

                //将主视图与切除图互换
//                List<Point> newMainPolygonPoints = Lists.newArrayList();
//                for(Point point : polygon.getPoints()){
//                    newMainPolygonPoints.add(point);
//                }
//                List<Point> newPolygonPoints = Lists.newArrayList();
//                for(Point point : mainPolygon.getPoints()){
//                    newPolygonPoints.add(point);
//                }
//                polygon.setPoints(newPolygonPoints);
//                polygon.reCompute();
//                mainPolygon.setPoints(newMainPolygonPoints);
//                mainPolygon.reCompute();
                if (allInPolygon) {
                    inMainPolygons.add(polygon);
                    polygonIterator.remove();
                    continue;
                }
                //处理
                reserve = true;
            }
            if (UtilMisc.isEmpty(inMainGraphPoints)) {
                //TODO 具体报错，后续处理 (如果报错的可能太多零件报错了，会导致拆解大量失败)
                //throw new BasicBusinessException("两个图形没有相交");
                continue;
            }
            //去除重复,并保持顺序
            Iterator<Point> pointIterator = inMainGraphPoints.iterator();
            Set<Point> newPoints = Sets.newHashSet();
            List<Point> newPointLists = Lists.newArrayList();
            while (pointIterator.hasNext()){
                Point point = pointIterator.next();
                if(newPoints.add(point)){
                    newPointLists.add(point);
                }
            }
            inMainGraphPoints.clear();
            inMainGraphPoints.addAll(newPointLists);
            if(!reserve){
                cutGtaph(polygon, mainPolygon, inMainGraphPoints);
            }else{
                cutGtaphReserve(polygon, mainPolygon, inMainGraphPoints);
            }
            polygonIterator.remove();
            hasNext = true;
            break;
        }
        if (hasNext) {
            cutAllGraph(mainPolygon, polygons, inMainPolygons);
        }
    }

    public static void cutGtaphReserve(Polygon polygon, Polygon mainPolygon, List<Point> inMainGraphPoints) {
        List<Point> newMainPolygonPoints = Lists.newArrayList();
        for (int index = 0; index < mainPolygon.getPoints().size(); index++) {
            Point point = mainPolygon.getPoints().get(index);
            Line line = mainPolygon.findLineByStartPoint(point);
            Point end = line.getEndPoint();
            if(!inMainGraphPoints.contains(point) && !inMainGraphPoints.contains(end)){
                newMainPolygonPoints.add(point);
            }else{
                if(inMainGraphPoints.contains(point) &&  !inMainGraphPoints.contains(end)){
                    //计算交点
                    Point interPoint = getLinePoinInPolygon(polygon,line);
                    if(UtilMisc.isNotNull(interPoint)){
                        newMainPolygonPoints.add(interPoint);
                    }
                }else if(!inMainGraphPoints.contains(point) && inMainGraphPoints.contains(end)){
                    newMainPolygonPoints.add(point);
                    //计算交点
                    Point interPoint = getLinePoinInPolygon(polygon,line);
                    if(UtilMisc.isNotNull(interPoint)){
                        newMainPolygonPoints.add(interPoint);
                    }
                }
            }
        }
        if (UtilMisc.isNotEmpty(newMainPolygonPoints)) {
            mainPolygon.setPoints(newMainPolygonPoints);
            mainPolygon.reCompute();
        }
    }

    public static Point getLinePoinInPolygon(Polygon polygon,Line line){
        for(Line line1 : polygon.getLines()){
            Point point1 = getLinePoint(line, line1);
            if(UtilMisc.isNotNull(point1)){
                return point1;
            }
        }
        return null;
    }

    public static void cutGtaph(Polygon polygon, Polygon mainPolygon, List<Point> inMainGraphPoints) {
        List<Point> newMainPolygonPoints = Lists.newArrayList();
        boolean isInclude = false;
        for (Point point : mainPolygon.getPoints()) {
            boolean isInPolygon = isInPolygon(point, polygon);
            if (!isInPolygon) {
                newMainPolygonPoints.add(point);
            } else {
                isInclude = true;
                if (UtilMisc.isNotEmpty(newMainPolygonPoints)) {
                    //判断上一个在不在
                    Line line = mainPolygon.findLineByEndPoint(point);
                    boolean lasPointIsInPolygon = isInPolygon(line.getStartPoint(), polygon);
                    if (!lasPointIsInPolygon) {
                        //上一个点不在，这个点在，则需要计算交点
                        Point focusPoint = getOnlyPoint(line, polygon, inMainGraphPoints);
                        newMainPolygonPoints.add(focusPoint);
                        newMainPolygonPoints.addAll(inMainGraphPoints);
                    }
                }
                //判断下一个在不在
                Line line = mainPolygon.findLineByStartPoint(point);
                boolean nextPointIsInPolygon = isInPolygon(line.getEndPoint(), polygon);
                if (!nextPointIsInPolygon) {
                    //下一个点不在，这个点在，则需要计算交点
                    Point focusPoint = getOnlyPoint(line, polygon, inMainGraphPoints);
                    newMainPolygonPoints.add(focusPoint);
                }
            }
        }
        //小矩形没有包含主视图的任何点
        if (!isInclude) {
            newMainPolygonPoints = Lists.newArrayList();
            for (int index = 0; index < mainPolygon.getPoints().size(); index++) {
                Point point = mainPolygon.getPoints().get(index);
                newMainPolygonPoints.add(point);
                if (index >= mainPolygon.getPoints().size() - 1) {
                    continue;
                }
                Line line = mainPolygon.findLineByStartPoint(point);
                Point end = line.getEndPoint();
                Line line1 = polygon.findLineByEndPoint(inMainGraphPoints.get(0));
                Point point1 = getLinePoint(line, line1);
                if (UtilMisc.isNull(point1)) {
                    Line line2 = polygon.findLineByStartPoint(inMainGraphPoints.get(0));
                    point1 = getLinePoint(line, line2);
                }
                if (UtilMisc.isNull(point1)) {
                    continue;
                }
                Line line3 = polygon.findLineByStartPoint(inMainGraphPoints.get(inMainGraphPoints.size() - 1));
                Point point2 = getLinePoint(line, line3);
                if (UtilMisc.isNull(point2)) {
                    Line line4 = polygon.findLineByEndPoint(inMainGraphPoints.get(inMainGraphPoints.size() - 1));
                    point2 = getLinePoint(line, line4);
                }
                if (UtilMisc.isNull(point2)) {
                    continue;
                }
                if (UtilNumber.equals(point1.getX(), point2.getX())
                        && UtilNumber.equals(point1.getY(), point2.getY())) {
                    newMainPolygonPoints.add(point1);
                    newMainPolygonPoints.addAll(inMainGraphPoints);
                } else {
                    if (UtilNumber.equals(point.getX(), end.getX())) {
                        double point2Y = Math
                                .abs(UtilNumber.numberSubstract(point2.getY(), point.getY()).doubleValue());
                        double point1Y = Math
                                .abs(UtilNumber.numberSubstract(point1.getY(), point.getY()).doubleValue());
                        if (point2Y < point1Y) {
                            newMainPolygonPoints.add(point2);
                            Collections.reverse(inMainGraphPoints);
                            newMainPolygonPoints.addAll(inMainGraphPoints);
                            newMainPolygonPoints.add(point1);
                        } else {
                            newMainPolygonPoints.add(point1);
                            newMainPolygonPoints.addAll(inMainGraphPoints);
                            newMainPolygonPoints.add(point2);
                        }
                    } else {
                        double point2X = Math
                                .abs(UtilNumber.numberSubstract(point2.getX(), point.getX()).doubleValue());
                        double point1X = Math
                                .abs(UtilNumber.numberSubstract(point1.getX(), point.getX()).doubleValue());
                        if (point2X < point1X) {
                            newMainPolygonPoints.add(point2);
                            Collections.reverse(inMainGraphPoints);
                            newMainPolygonPoints.addAll(inMainGraphPoints);
                            newMainPolygonPoints.add(point1);
                        } else {
                            newMainPolygonPoints.add(point1);
                            newMainPolygonPoints.addAll(inMainGraphPoints);
                            newMainPolygonPoints.add(point2);
                        }
                    }
                }
            }
        }
        if (UtilMisc.isNotEmpty(newMainPolygonPoints)) {
            mainPolygon.setPoints(newMainPolygonPoints);
            mainPolygon.reCompute();
        }
    }

    /**
     * 计算交点
     *
     * @param line
     * @param polygon
     * @param inMainGraphPoints
     * @return
     */
    public static Point getOnlyPoint(Line line, Polygon polygon, List<Point> inMainGraphPoints) {
        Line line1 = polygon.findLineByEndPoint(inMainGraphPoints.get(0));
        Point result = getLinePoint(line, line1);
        if (UtilMisc.isNotNull(result)) {
            return result;
        }
        Line line2 = polygon.findLineByStartPoint(inMainGraphPoints.get(0));
        result = getLinePoint(line, line2);
        if (UtilMisc.isNotNull(result)) {
            return result;
        }
        Line line3 = polygon.findLineByStartPoint(inMainGraphPoints.get(inMainGraphPoints.size() - 1));
        result = getLinePoint(line, line3);
        if (UtilMisc.isNotNull(result)) {
            return result;
        }
        Line line4 = polygon.findLineByEndPoint(inMainGraphPoints.get(inMainGraphPoints.size() - 1));
        result = getLinePoint(line, line4);
        if (UtilMisc.isNotNull(result)) {
            return result;
        }
        return null;
    }

    /**
     * 初始化SVG中所有的封闭图形（目前只针对多变形）
     *
     * @param parentElement
     * @param polygons
     * @param mainPolygon
     */
    public static Polygon initPolygon(Element parentElement, List<Polygon> polygons, List<Element> notNeedHandle,
                                      boolean isFindMain) {
        List<Element> listElements = parentElement.elements();
        if (UtilMisc.isEmpty(listElements)) {
            return null;
        }
        for (Element element : listElements) {
            if (element.getName().equals("path") && !element.attributes().isEmpty()
                    && UtilMisc.isNotNull(element.attribute("d"))) {
                String d = element.attribute("d").getValue().replaceAll("M", "L");
                if (d.indexOf("A") != -1 || d.indexOf("a") != -1) {
                    //还有曲线，不处理
                    notNeedHandle.add(element);
                    continue;
                }
                Polygon polygon = new Polygon();
                String[] pointStrs = d.split("L");
                List<Point> points = Lists.newArrayList();
                for (String pointStr : pointStrs) {
                    if (UtilMisc.isEmpty(pointStr)) {
                        continue;
                    }
                    pointStr = pointStr.trim();
                    String[] pointXY = pointStr.split(" ");
                    BigDecimal pointX = new BigDecimal(pointXY[0]);//.setScale(1,BigDecimal.ROUND_HALF_UP);
                    BigDecimal pointY = new BigDecimal(pointXY[1]);//.setScale(1,BigDecimal.ROUND_HALF_UP);
                    Point point = new Point();
                    point.setX(pointX);
                    point.setY(pointY);
                    points.add(point);
                }
                polygon.setPoints(points);
                polygon.reCompute();
                if (isFindMain) {
                    if (!parentElement.attributes().isEmpty() && UtilMisc.isNotNull(parentElement.attribute("id"))
                            && parentElement.attribute("id").getValue().equals("Sketch")) {
                        return polygon;
                    }
                } else {
                    if (!parentElement.attributes().isEmpty() && UtilMisc.isNotNull(parentElement.attribute("id"))
                            && parentElement.attribute("id").getValue().equals("Sketch")) {
                        continue;
                    }
                    //判断是否是一条线：
                    boolean isLine = false;
                    for (Line line : polygon.getLines()) {
                        Point startPoint = line.getStartPoint();
                        Point endPoint = line.getEndPoint();
                        BigDecimal tmpX = new BigDecimal(
                                Math.abs(UtilNumber.numberSubstract(endPoint.getX(), startPoint.getX()).doubleValue()));
                        BigDecimal tmpY = new BigDecimal(
                                Math.abs(UtilNumber.numberSubstract(endPoint.getY(), startPoint.getY()).doubleValue()));
                        BigDecimal tmpLength = new BigDecimal(Math.sqrt(
                                UtilNumber.numberAdd(UtilNumber.numberMultiply(tmpX, tmpX), UtilNumber.numberMultiply(tmpY, tmpY))
                                        .doubleValue()));
                        if(UtilNumber.lessEqualThan(tmpLength,new BigDecimal(2))){
                            isLine = true;
                            break;
                        }
                    }
                    if(!isLine){
                        polygons.add(polygon);
                    }
                }
            }
            if (element.getName().equals("g")) {
                initPolygon(element, polygons, notNeedHandle, isFindMain);
            } else if (!element.getName().equals("path")) {
                notNeedHandle.add(element);
            }
        }
        return null;
    }

    /**
     * 获取直线的倾斜角
     *
     * @param line
     * @return
     */
    public static BigDecimal getLineAngle(Line line) {
        //由直线的两点式转化为一般式：
        //x(y2-y1)+y(x1-x2) = x1(y2-y1)+y1(x1-x2)  -->  a1x + b1y = c
        BigDecimal a1 = UtilNumber.numberSubstract(line.getEndPoint().getY(), line.getStartPoint().getY());
        BigDecimal b1 = UtilNumber.numberSubstract(line.getStartPoint().getX(), line.getEndPoint().getX());
        //tanA = -a/b
        //A = arctan(-a/b)
        if (UtilNumber.isZero(b1)) {
            return new BigDecimal(90);
        }
        BigDecimal tmp = UtilNumber.numberDivideZero(UtilNumber.numberSubstract(BigDecimal.ZERO, a1), b1);
        return new BigDecimal(Math.toDegrees(Math.atan(tmp.doubleValue())));
    }

    /**
     * 获取弧线所在椭圆与直线的交点
     *
     * @param arcLine
     * @param line
     * @param isGetStart 是否获取距离起点较近的交点，若为false，则获取距离弧线终点较近的交点
     * @return
     */
    public static Point getArcLinePoint(Line arcLine, Line line, BigDecimal sawWidth, boolean isGetStart)
            throws Exception {
        //椭圆方程：（x-cx）^2/rx^2 + (y-cy)^2/ry^2 = 1
        //直线方程：x(y2-y1)+y(x1-x2) = x1(y2-y1)+y1(x1-x2)  -->  a1x + b1y = c1
        BigDecimal a1 = UtilMisc.isNotNull(line.getA()) ? line.getA() : BigDecimal.ZERO;
        BigDecimal b1 = UtilMisc.isNotNull(line.getB()) ? line.getB() : BigDecimal.ZERO;
        BigDecimal c1 = UtilMisc.isNotNull(line.getC()) ? line.getC() : BigDecimal.ZERO;
        if (UtilMisc.isNull(line.getA()) && UtilMisc.isNull(line.getB()) && UtilMisc.isNull(line.getC())) {
            a1 = UtilNumber.numberSubstract(line.getEndPoint().getY(), line.getStartPoint().getY());
            b1 = UtilNumber.numberSubstract(line.getStartPoint().getX(), line.getEndPoint().getX());
            c1 = UtilNumber.numberAdd(UtilNumber.numberMultiply(line.getStartPoint().getX(), a1),
                    UtilNumber.numberMultiply(line.getStartPoint().getY(), b1));
        }
        BigDecimal ba = UtilNumber.numberDivideZero(b1, a1);
        BigDecimal ca = UtilNumber.numberDivideZero(c1, a1);

        BigDecimal ab = UtilNumber.numberDivideZero(a1, b1);
        BigDecimal cb = UtilNumber.numberDivideZero(c1, b1);

        SvgArcObject svgArcObject = SvgArcUtil.svgArcToCenterParam(arcLine.getStartPoint().getX(),
                arcLine.getStartPoint().getY(), arcLine.getRx(), arcLine.getRy(), arcLine.getX_axis_rotation(),
                arcLine.getLarge_arc_flag(), arcLine.getSweep_flag(), arcLine.getEndPoint().getX(),
                arcLine.getEndPoint().getY());
        BigDecimal cx = svgArcObject.getCx();
        BigDecimal cy = svgArcObject.getCy();

        BigDecimal rx = UtilNumber.numberAdd(arcLine.getRx(), sawWidth);
        BigDecimal ry = UtilNumber.numberAdd(arcLine.getRy(), sawWidth);
        if (arcLine.getIsArcInwards()) {
            rx = UtilNumber.numberSubstract(arcLine.getRx(), sawWidth);
            ry = UtilNumber.numberSubstract(arcLine.getRy(), sawWidth);
        }
        BigDecimal rx2 = UtilNumber.numberMultiply(rx, rx);
        BigDecimal ry2 = UtilNumber.numberMultiply(ry, ry);

        BigDecimal x1 = null;
        BigDecimal y1 = null;
        BigDecimal x2 = null;
        BigDecimal y2 = null;

        if (UtilNumber.equals(b1, BigDecimal.ZERO)) {
            //解二元二次方程组：
            // (b1^2/a1^2)*ry^2 + rx^2 = a
            // -((2*b1/a1)*((c1/a1)-cx)*ry^2 + 2*cy*rx^2) = b
            // rx^2*ry^2 - ((c1/a1)-cx)^2*ry^2-cy^2*rx^2 = -c
            BigDecimal a = UtilNumber.numberAdd(UtilNumber.numberMultiply(ba, ba, ry2), rx2);
            BigDecimal b = UtilNumber
                    .numberAdd(UtilNumber.numberMultiply(new BigDecimal(2), ba, UtilNumber.numberSubstract(ca, cx), ry2),
                            UtilNumber.numberMultiply(new BigDecimal(2), cy, rx2))
                    .negate();
            BigDecimal c = UtilNumber.numberSubstract(UtilNumber.numberMultiply(rx2, ry2),
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(ca, cx), UtilNumber.numberSubstract(ca, cx), ry2),
                    UtilNumber.numberMultiply(cy, cy, rx2)).negate();
            // 转化为标准式：a*y2 + b*y + c = 0  △ = b^2 -4*a*c ,
            //若△< 0 则方程没有实数根；若△=0，则有一个解；若△> 0 ，则有两个截
            BigDecimal daerta = UtilNumber.numberSubstract(UtilNumber.numberMultiply(b, b),
                    UtilNumber.numberMultiply(new BigDecimal(4), a, c));
            if (UtilNumber.lessThan(daerta, BigDecimal.ZERO)) {
                return null;
            }
            // y1 = （-b + 根号 △）/ 2*a
            // y2 = （-b - 根号 △）/ 2*a
            y1 = UtilNumber.numberDivideZero(
                    UtilNumber.numberAdd(b.negate(), new BigDecimal(Math.sqrt(daerta.doubleValue()))),
                    UtilNumber.numberMultiply(new BigDecimal(2), a));
            y2 = UtilNumber.numberDivideZero(
                    UtilNumber.numberSubstract(b.negate(), new BigDecimal(Math.sqrt(daerta.doubleValue()))),
                    UtilNumber.numberMultiply(new BigDecimal(2), a));

            //将y1，y2带入直线方程：
            // x = -(b1/a1)*y + c1/a1
            x1 = UtilNumber.numberAdd(UtilNumber.numberMultiply(ba.negate(), y1), ca);
            x2 = UtilNumber.numberAdd(UtilNumber.numberMultiply(ba.negate(), y2), ca);
        } else {
            //解二元二次方程组：
            // (a1^2/b1^2)*rx^2 + ry^2 = a
            // -((2*a1/b1)*((c1/b1)-cy)*rx^2 + 2*cx*ry^2) = b
            // rx^2*ry^2 - ((c1/b1)-cy)^2*rx^2-cx^2*ry^2 = -c
            BigDecimal a = UtilNumber.numberAdd(UtilNumber.numberMultiply(ab, ab, rx2), ry2);
            BigDecimal b = UtilNumber
                    .numberAdd(UtilNumber.numberMultiply(new BigDecimal(2), ab, UtilNumber.numberSubstract(cb, cy), rx2),
                            UtilNumber.numberMultiply(new BigDecimal(2), cx, ry2))
                    .negate();
            BigDecimal c = UtilNumber.numberSubstract(UtilNumber.numberMultiply(rx2, ry2),
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(cb, cy), UtilNumber.numberSubstract(cb, cy), rx2),
                    UtilNumber.numberMultiply(cx, cx, ry2)).negate();
            // 转化为标准式：a*x2 + b*x + c = 0  △ = b^2 -4*a*c ,
            //若△< 0 则方程没有实数根；若△=0，则有一个解；若△> 0 ，则有两个截
            BigDecimal daerta = UtilNumber.numberSubstract(UtilNumber.numberMultiply(b, b),
                    UtilNumber.numberMultiply(new BigDecimal(4), a, c));
            if (UtilNumber.lessThan(daerta, BigDecimal.ZERO)) {
                return null;
            }
            // x1 = （-b + 根号 △）/ 2*a
            // x2 = （-b - 根号 △）/ 2*a
            x1 = UtilNumber.numberDivideZero(
                    UtilNumber.numberAdd(b.negate(), new BigDecimal(Math.sqrt(daerta.doubleValue()))),
                    UtilNumber.numberMultiply(new BigDecimal(2), a));
            x2 = UtilNumber.numberDivideZero(
                    UtilNumber.numberSubstract(b.negate(), new BigDecimal(Math.sqrt(daerta.doubleValue()))),
                    UtilNumber.numberMultiply(new BigDecimal(2), a));

            //将x1，x2带入直线方程：
            // y = -(a1/b1)*y + c1/b1
            y1 = UtilNumber.numberAdd(UtilNumber.numberMultiply(ab.negate(), x1), cb);
            y2 = UtilNumber.numberAdd(UtilNumber.numberMultiply(ab.negate(), x2), cb);
        }

        if (isGetStart) {
            //得到与起点最近的一个点：
            BigDecimal length1 = UtilNumber.numberAddZero(
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(x1, arcLine.getStartPoint().getX()),
                            UtilNumber.numberSubstract(x1, arcLine.getStartPoint().getX())),
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(y1, arcLine.getStartPoint().getY()),
                            UtilNumber.numberSubstract(y1, arcLine.getStartPoint().getY())));

            BigDecimal length2 = UtilNumber.numberAddZero(
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(x2, arcLine.getStartPoint().getX()),
                            UtilNumber.numberSubstract(x2, arcLine.getStartPoint().getX())),
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(y2, arcLine.getStartPoint().getY()),
                            UtilNumber.numberSubstract(y2, arcLine.getStartPoint().getY())));

            Point point1 = new Point();
            point1.setX(x1);
            point1.setY(y1);

            Point point2 = new Point();
            point2.setX(x2);
            point2.setY(y2);

            if (UtilNumber.lessEqualThan(length1, length2)) {
                return point1;
            } else {
                return point2;
            }
        } else {
            //得到距离终点较近的一点
            BigDecimal length1 = UtilNumber.numberAddZero(
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(x1, arcLine.getEndPoint().getX()),
                            UtilNumber.numberSubstract(x1, arcLine.getEndPoint().getX())),
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(y1, arcLine.getEndPoint().getY()),
                            UtilNumber.numberSubstract(y1, arcLine.getEndPoint().getY())));

            BigDecimal length2 = UtilNumber.numberAddZero(
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(x2, arcLine.getEndPoint().getX()),
                            UtilNumber.numberSubstract(x2, arcLine.getEndPoint().getX())),
                    UtilNumber.numberMultiply(UtilNumber.numberSubstract(y2, arcLine.getEndPoint().getY()),
                            UtilNumber.numberSubstract(y2, arcLine.getEndPoint().getY())));

            Point point1 = new Point();
            point1.setX(x1);
            point1.setY(y1);

            Point point2 = new Point();
            point2.setX(x2);
            point2.setY(y2);

            if (UtilNumber.lessEqualThan(length1, length2)) {
                return point1;
            } else {
                return point2;
            }
        }
    }

    /**
     * 获取两条线段的交点
     *
     * @param line1 主图线段
     * @param line2 附图线段
     */
    public static Point getLinePoint(Line line1, Line line2) {
        //由直线的两点式转化为一般式：
        //x(y2-y1)+y(x1-x2) = x1(y2-y1)+y1(x1-x2)  -->  a1x + b1y = c
        BigDecimal a1 = UtilNumber.numberSubstract(line1.getEndPoint().getY(), line1.getStartPoint().getY());
        BigDecimal b1 = UtilNumber.numberSubstract(line1.getStartPoint().getX(), line1.getEndPoint().getX());
        BigDecimal c1 = UtilNumber.numberAdd(UtilNumber.numberMultiply(line1.getStartPoint().getX(), a1),
                UtilNumber.numberMultiply(line1.getStartPoint().getY(), b1));
        //第二条直线
        BigDecimal a2 = UtilNumber.numberSubstract(line2.getEndPoint().getY(), line2.getStartPoint().getY());
        BigDecimal b2 = UtilNumber.numberSubstract(line2.getStartPoint().getX(), line2.getEndPoint().getX());
        BigDecimal c2 = UtilNumber.numberAdd(UtilNumber.numberMultiply(line2.getStartPoint().getX(), a2),
                UtilNumber.numberMultiply(line2.getStartPoint().getY(), b2));
        //解该二元一次方程：
        //y = (c1*a2-c2*a1)/(b1*a2 - b2*a1)
        //x = (b1*c2-b2*c1)/(b1*a2 - b2*a1)
        //若b1*a2 - b2*a1 != 0 则有交点
        //若b1*a2 - b2*a1 == 0 且 c1*a2-c2*a1 == 0 则有无数解
        //若b1*a2 - b2*a1 == 0 且 c1*a2-c2*a1 != 0 则有无解
        BigDecimal tmpResult = UtilNumber.numberSubstract(UtilNumber.numberMultiply(b1, a2),
                UtilNumber.numberMultiply(b2, a1));
        BigDecimal tmpYResult = UtilNumber.numberSubstract(UtilNumber.numberMultiply(c1, a2),
                UtilNumber.numberMultiply(c2, a1));
        BigDecimal tmpXResult = UtilNumber.numberSubstract(UtilNumber.numberMultiply(b1, c2),
                UtilNumber.numberMultiply(b2, c1));
        if (!UtilNumber.equals(tmpResult, BigDecimal.ZERO)) {
            BigDecimal y = UtilNumber.numberDivide(tmpYResult, tmpResult);
            BigDecimal x = UtilNumber.numberDivide(tmpXResult, tmpResult);

            //            Point newPoint = new Point();
            //            newPoint.setY(y);
            //            newPoint.setX(x);
            //            return newPoint;

            //交点的左边范围在辅助线的范围内
            Point startPoint = line2.getStartPoint();
            Point endPoint = line2.getEndPoint();
            BigDecimal minX = startPoint.getX();
            BigDecimal minY = startPoint.getY();
            BigDecimal maxX = endPoint.getX();
            BigDecimal maxY = endPoint.getY();
            if (UtilNumber.lessThan(endPoint.getX(), minX)) {
                minX = endPoint.getX();
                maxX = startPoint.getX();
            }
            if (UtilNumber.lessThan(endPoint.getY(), minY)) {
                minY = endPoint.getY();
                maxY = startPoint.getY();
            }
            if (UtilNumber.greaterEqualThan(x, minX) && UtilNumber.greaterEqualThan(y, minY)
                    && UtilNumber.lessEqualThan(x, maxX) && UtilNumber.lessEqualThan(y, maxY)) {
                Point newPoint = new Point();
                newPoint.setY(y);
                newPoint.setX(x);
                return newPoint;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 判断一个点是否在某个封闭的多边形中
     *
     * @param point 点的坐标
     * @param polygon 多边形
     * @return
     */
    public static boolean isInPolygon(Point point, Polygon polygon) {
        boolean isInPolygon = true;
        //方法是，由当前点做射线，如果射线与多边形的各边交点是偶数个，则在多边形外；否则在多边形内
        //射线与与线段(v1,v2：v1是较低的点；v2是较高的点)相交有两个条件：
        //1. point的y必须在线段点的y值之间
        //2. 判断该点在线段的左侧还是右侧，如果在线段的左侧，则相交。
        //3. 判断是在线段的左侧，则需要求出线段与射线的交点的x坐标：x = (point.y-v1.y)*(v2.x-v1.x)/(v2.y-v1.y)+v1.x
        List<Line> lines = polygon.getLines();
        int size = 0;
        for (Line line : lines) {
            Point v1 = line.getStartPoint();
            Point v2 = line.getEndPoint();
            if (UtilNumber.lessThan(line.getEndPoint().getY(), line.getStartPoint().getY())) {
                v1 = line.getEndPoint();
                v2 = line.getStartPoint();
            }
            //point的y在线段点的y值之间
            if (UtilNumber.greaterThan(point.getY(), v1.getY()) && UtilNumber.lessEqualThan(point.getY(), v2.getY())) {
                BigDecimal tmpX = UtilNumber.numberAdd(UtilNumber.numberDivide(
                        UtilNumber.numberMultiply(UtilNumber.numberSubstract(point.getY(), v1.getY()),
                                UtilNumber.numberSubstract(v2.getX(), v1.getX())),
                        UtilNumber.numberSubstract(v2.getY(), v1.getY())), v1.getX());
                //在线段右侧
                if (UtilNumber.lessThan(point.getX(), tmpX)) {
                    size++;
                }
            }
        }
        if (size % 2 == 0) {
            isInPolygon = false;
        }
        return isInPolygon;
    }

    /**
     * 在子集中按ID查找节点
     *
     * @param parentElement
     * @param id
     * @return
     */
    public static Element findObjectByID(Element parentElement, String id) {
        List<Element> listElements = parentElement.elements();
        if (UtilMisc.isEmpty(listElements)) {
            return null;
        }
        Element object = null;
        for (Element element : listElements) {
            if (!element.attributes().isEmpty() && UtilMisc.isNotNull(element.attribute("id"))) {
                if (element.attribute("id").getValue().equals(id)) {
                    object = element;
                    break;
                }
            }
            object = findObjectByID(element, id);
            if (UtilMisc.isNotNull(object)) {
                break;
            }
        }
        return object;
    }
}
