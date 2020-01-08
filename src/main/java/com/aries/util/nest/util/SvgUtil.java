package com.aries.util.nest.util;

import com.aries.util.nest.data.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SvgUtil {

    public static List<String> svgGenerator(List<NestPath> list, List<List<Placement>> applied, double binwidth, double binHeight) throws Exception {
        List<String> strings = new ArrayList<String>();
        int x = 10;
        int y = 0;
        for (List<Placement> binlist : applied) {
            String s = " <g transform=\"translate(" + x + "  " + y + ")\">" + "\n";
            s += "    <rect x=\"0\" y=\"0\" width=\"" + binwidth + "\" height=\"" + binHeight + "\"  fill=\"none\" stroke=\"#010101\" stroke-width=\"1\" />\n";
            for (Placement placement : binlist) {
                int bid = placement.bid;
                NestPath nestPath = getNestPathByBid(bid, list);
                double ox = placement.translate.x;
                double oy = placement.translate.y;
                double rotate = placement.rotate;
                s += "<g transform=\"translate(" + ox + x + " " + oy + y + ") rotate(" + rotate + ")\"> \n";
                s += "<path d=\"";
                for (int i = 0; i < nestPath.getSegments().size(); i++) {
                    if (i == 0) {
                        s += "M";
                    } else {
                        s += "L";
                    }
                    Segment segment = nestPath.get(i);
                    s += segment.x + " " + segment.y + " ";
                }
                s += "Z\" fill=\"#8498d1\" stroke=\"#010101\" stroke-width=\"1\" />" + " \n";
                s += "</g> \n";
            }
            s += "</g> \n";
            y += binHeight + 50;
            strings.add(s);
        }
        return strings;
    }

    private static NestPath getNestPathByBid(int bid, List<NestPath> list) {
        for (NestPath nestPath : list) {
            if (nestPath.bid == bid) {
                return nestPath;
            }
        }
        return null;
    }


    /**
     * 圆的参数方程
     * x = a + r*cos Angle
     * y = b + r*sin Angle
     * <p>
     * Angle 取值范围 [0,2PI]
     *
     * @param cx
     * @param cy
     * @param r
     * @return
     */
    public static List<Point> circleToPoints(double cx, double cy, double r) {
        List<Point> points = new ArrayList<>();
        BigDecimal tmpX = null;
        BigDecimal tmpY = null;
        Point tmpPoint = null;
        for (double angle = 0; angle <= 360; angle += 1) {
            tmpPoint = new Point();
            tmpX = new BigDecimal(cx + r * Math.cos(Math.toRadians(angle)));
            tmpY = new BigDecimal(cy + r * Math.sin(Math.toRadians(angle)));
            tmpPoint.setX(tmpX);
            tmpPoint.setY(tmpY);

            points.add(tmpPoint);
        }
        return points;
    }


    /**
     * 椭圆的参数方程
     * x = a + rx*cos Angle
     * y = b + ry*sin Angle
     * <p>
     * Angle 取值范围 [0,2PI]
     *
     * @param cx
     * @param cy
     * @param rx
     * @param ry
     * @param startAngle
     * @param endAngle
     * @return
     */
    public static List<Point> ellipseToPoints(double cx, double cy, double rx, double ry, double startAngle, double endAngle) {
        List<Point> points = new ArrayList<>();
        BigDecimal tmpX = null;
        BigDecimal tmpY = null;
        Point tmpPoint = null;
        for (double angle = startAngle; angle <= endAngle; angle += 1) {
            tmpPoint = new Point();
            tmpX = new BigDecimal(cx + rx * Math.cos(Math.toRadians(angle)));
            tmpY = new BigDecimal(cy + ry * Math.sin(Math.toRadians(angle)));
            tmpPoint.setX(tmpX);
            tmpPoint.setY(tmpY);
            points.add(tmpPoint);
        }
        return points;
    }

    //根据弧线路径计算中心点坐标
    //https://blog.csdn.net/cuixiping/article/details/7958298

    /**
     * 弧线转为点集
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param rx
     * @param ry
     * @param xAxisRotation
     * @param largeArcFlag
     * @param sweepFlag
     * @return
     */
    public static List<Point> arcToPoints(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2, BigDecimal rx,
                                          BigDecimal ry, BigDecimal xAxisRotation, BigDecimal largeArcFlag, BigDecimal sweepFlag) {

        //将SVG转为多个贝塞尔三次曲线   svg-arc-to-cubic-bezier-master
        List<Bezier> beziers = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        BigDecimal px = x1;
        BigDecimal py = y1;
        Bezier lastBezier = null;
        for (Bezier bezier : beziers) {
            if (lastBezier != null) {
                px = lastBezier.getX();
                py = lastBezier.getY();
            }
            points.addAll(bezierToPoints(px, py, bezier));
        }
        return points;
    }

    /**
     * 将三次贝塞尔曲线转为点集
     * 三次贝塞尔曲线公式：
     * <p>
     * B(t) = P0*(1-t)`3 + 3*P1*t*(1-t)`2 + 3*P2*t`2*(1-t) + P3*t`3
     * <p>
     * t的取值范围 [0,1]
     *
     * @param px
     * @param py
     * @param bezier
     * @return
     */
    public static List<Point> bezierToPoints(BigDecimal px, BigDecimal py, Bezier bezier) {
        List<Point> points = new ArrayList<>();
        BigDecimal tmpT = null;
        BigDecimal tmpT_1 = null;
        for (double t = 0; t <= 1; t += 0.01) {
            tmpT = new BigDecimal(t);
            tmpT_1 = BigDecimal.ONE.subtract(tmpT);
//            BigDecimal tmpX =getBezierValue(getBezierValue(px,bezier.getX1(),bezier.getX2(),bezier.getX(),tmpT,tmpT_1));
        }
        return points;
    }


    private static BigDecimal getBezierValue(BigDecimal p0, BigDecimal p1, BigDecimal p2, BigDecimal p3, BigDecimal tmpT, BigDecimal tmpT_1) {
        BigDecimal tmpP0 = p0.multiply(tmpT_1).multiply(tmpT_1).multiply(tmpT_1);

        return tmpP0;
    }

}
