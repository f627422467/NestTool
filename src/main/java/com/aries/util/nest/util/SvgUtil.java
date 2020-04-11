package com.aries.util.nest.util;

import com.aries.util.nest.data.*;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SvgUtil {


    public static List<String> svgGenerator(List<NestPath> list, List<List<Placement>> applied, double binwidth, double binHeight) throws Exception {
        List<String> strings = new ArrayList<String>();
        int x = 10;
        int y = 0;

        //针对于圆形，只能是画图时使用path。转成NestPath时构造成矩形。内部的孔添加属性，填充颜色时不上色
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


    public static List<String> beforSvgNest(List<NestPath> list, double binwidth, double binHeight){
        List<String> strings = new ArrayList<String>();
        int x = 10;
        int y = 0;

        //针对于圆形，只能是画图时使用path。转成NestPath时构造成矩形。内部的孔添加属性，填充颜色时不上色
        String s = " <g transform=\"translate(" + x + "  " + y + ")\">" + "\n";
        s += "    <rect x=\"0\" y=\"0\" width=\"" + binwidth + "\" height=\"" + binHeight + "\"  fill=\"none\" stroke=\"#010101\" stroke-width=\"1\" />\n";
        for (NestPath nestPath : list) {
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
        }
        s += "</g> \n";
        y += binHeight + 50;
        strings.add(s);
        return strings;
    }

    /**
     * 通过Bid查找Nest
     * @param bid
     * @param list
     * @return
     */
    public static NestPath getNestPathByBid(int bid, List<NestPath> list) {
        for (NestPath nestPath : list) {
            if (nestPath.bid == bid) {
                return nestPath;
            }
        }
        return null;
    }

    /**
     *  计算SVG中旋转后的真实点坐标
     * @param x 原x
     * @param y 原y
     * @param x0 translate 中的x
     * @param y0 translate 中的y
     * @param angle rotate 的角度
     * @return
     *
     * 直角坐标系中，任意一点（x，y）饶另一点（x0，y0），逆时针旋转angle角度后的新坐标x1,y1
     * x1 = (x-x0)*cos angle - (y-y0)*sin angle + x0
     * y1 = (x-xo)*sin angle + (y-y0)*cons angle + y0
     */
    public static SVGPoint getSVGRotatePoint(double x, double y, double x0, double y0, double angle) {
        SVGPoint svgPoint = new SVGPoint();
        double tmpx = x * Math.cos(Math.toRadians(angle)) - y * Math.sin(Math.toRadians(angle)) + x0;
        double tmpy = x * Math.sin(Math.toRadians(angle)) + y * Math.cos(Math.toRadians(angle)) + y0;
        svgPoint.setX(new BigDecimal(tmpx));
        svgPoint.setY(new BigDecimal(tmpy));
        return svgPoint;
    }

    /**
     * 圆的参数方程：
     * x = a + r*cos Angle
     * y = b + r*sin Angle
     *
     * (a,b) 圆心坐标
     * Angle [0,2PI]
     * @return
     */

    public static String circleToPoints(double cx,double cy, double r){
        String result = "";
        BigDecimal tmpx = null;
        BigDecimal tmpy = null;
        for (double angle = 0; angle <= 360; angle += 1) {
            tmpx = new BigDecimal(cx + r*Math.cos(Math.toRadians(angle)));
            tmpy = new BigDecimal(cy + r*Math.sin(Math.toRadians(angle)));
            result += setPointStr(tmpx, tmpy);
        }
        return result;
    }

    /**
     * 椭圆的参数方程
     * x = a + rx*cos Angle
     * y = b + ry*sin Angle
     *
     * (a,b) 圆心坐标
     * Angle [0,2PI]
     *
     */
    public static String ellipseToPoints(double cx, double cy, double rx, double ry, double startAngle,
                                         double endAngle) {
        String result = "";
        BigDecimal tmpx = null;
        BigDecimal tmpy = null;
        for (double angle = startAngle; angle <= endAngle; angle += 1) {
            tmpx = new BigDecimal(cx + rx * Math.cos(Math.toRadians(angle)));
            tmpy = new BigDecimal(cy + ry * Math.sin(Math.toRadians(angle)));
            result += setPointStr(tmpx, tmpy);
        }
        return result;
    }

    /**
     * 将弧线转化为点集
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param rx
     * @param ry
     * @param xAxisRotation
     * @param large_arc_flag
     * @param sweep_flag
     * @return
     */
    public static String arcToPoints(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2, BigDecimal rx,
                                     BigDecimal ry, BigDecimal xAxisRotation, BigDecimal large_arc_flag, BigDecimal sweep_flag) {
        List<Bezier> beziers = SvgArcUtil.arcToBezier(x1.doubleValue(), y1.doubleValue(), x2.doubleValue(),
                y2.doubleValue(), rx.doubleValue(), ry.doubleValue(), xAxisRotation, large_arc_flag, sweep_flag);
        //将贝塞尔三次曲线转化成点集
        BigDecimal px = x1;
        BigDecimal py = y1;
        Bezier lastBezier = null;
        String points = "";
        for (Bezier bezier : beziers) {
            if(UtilMisc.isNotNull(lastBezier)){
                px = lastBezier.getX();
                py = lastBezier.getY();
            }
            points += SvgUtil.bezierToPoint(px,py,bezier);
            lastBezier = bezier;
        }
        return points;
    }

    /**
     * 将三次贝塞尔曲线转化为点集
     * 三次贝塞尔曲线点坐标公式：
     *
     * B(t) = P0*(1-t)^3 + 3*P1*t*(1-t)^2 + 3*P2*t^2*(1-t) + P3*t^3 ; t 取值范围 [0,1]
     */
    public static String bezierToPoint(BigDecimal px, BigDecimal py, Bezier bezier) {
        //+= 0.01 是100个点；如果取更多的点，则需要更小
        BigDecimal tmpT = null;
        BigDecimal tmpT_1 = null;
        String result = "";
        for (double t = 0; t <= 1; t += 0.01) {
            tmpT = new BigDecimal(t);
            tmpT_1 = UtilNumber.numberSubstract(BigDecimal.ONE, tmpT);
            BigDecimal tmpx = getBezierValue(px, bezier.getX1(), bezier.getX2(), bezier.getX(), tmpT, tmpT_1)
                    .setScale(4, BigDecimal.ROUND_HALF_UP);
            BigDecimal tmpy = getBezierValue(py, bezier.getY1(), bezier.getY2(), bezier.getY(), tmpT, tmpT_1)
                    .setScale(4, BigDecimal.ROUND_HALF_UP);
            result += setPointStr(tmpx, tmpy);
        }
        return result;
    }

    private static BigDecimal getBezierValue(BigDecimal p0, BigDecimal p1, BigDecimal p2, BigDecimal p3,
                                             BigDecimal tmpT, BigDecimal tmpT_1) {
        BigDecimal tmpP0 = UtilNumber.numberMultiply(p0, tmpT_1, tmpT_1, tmpT_1);
        BigDecimal tmpP1 = UtilNumber.numberMultiply(new BigDecimal(3), p1, tmpT, tmpT_1, tmpT_1);
        BigDecimal tmpP2 = UtilNumber.numberMultiply(new BigDecimal(3), p2, tmpT, tmpT, tmpT_1);
        BigDecimal tmpP3 = UtilNumber.numberMultiply(p3, tmpT, tmpT, tmpT);
        return UtilNumber.numberAdd(tmpP0, tmpP1, tmpP2, tmpP3);
    }



    /**
     * 判断点是否在椭圆内
     * （x-x1）^2/A^2 + (y-y1)^2/B^2 < 1 在椭圆内
     * （x-x1）^2/A^2 + (y-y1)^2/B^2 = 1 在椭圆上
     * （x-x1）^2/A^2 + (y-y1)^2/B^2 > 1 在椭圆外
     *
     * @param cx 焦点
     * @param cy 焦点
     * @param rx 长半轴
     * @param ry 短半轴
     * @return
     */
    public static BigDecimal getTmpY(BigDecimal cx, BigDecimal cy, BigDecimal rx, BigDecimal ry,BigDecimal x){
        try {
            BigDecimal tmpX = UtilNumber.numberSubstract(x, cx);
            BigDecimal tmp = UtilNumber.numberSubstract(BigDecimal.ONE,
                    UtilNumber.numberDivideZero(UtilNumber.numberMultiply(tmpX, tmpX), UtilNumber.numberMultiply(rx, rx)));
            tmp = UtilNumber.numberMultiply(tmp, UtilNumber.numberMultiply(ry, ry));
            tmp = new BigDecimal(Math.sqrt(tmp.doubleValue()));
            return UtilNumber.numberAdd(tmp, cy);
        }catch (Exception e){
            return null;
        }
    }

    public static BigDecimal getTmpX (BigDecimal cx, BigDecimal cy, BigDecimal rx, BigDecimal ry,BigDecimal y){
        try {
            BigDecimal tmpY = UtilNumber.numberSubstract(y, cx);
            BigDecimal tmp = UtilNumber.numberSubstract(BigDecimal.ONE,
                    UtilNumber.numberDivideZero(UtilNumber.numberMultiply(tmpY, tmpY), UtilNumber.numberMultiply(ry, ry)));
            tmp = UtilNumber.numberMultiply(tmp, UtilNumber.numberMultiply(rx, rx));
            tmp = new BigDecimal(Math.sqrt(tmp.doubleValue()));
            return UtilNumber.numberAdd(tmp, cx);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 弧线转多边形 (点集)
     * @param startXPre 起点坐标的前一个坐标
     * @param startYPre 起点坐标的前一个坐标
     * @param x1 起点坐标
     * @param y1 起点坐标
     * @param x2 终点坐标
     * @param y2 终点坐标
     * @param endXPost 终点坐标的后一个坐标
     * @param endYPost 终点坐标的后一个坐标
     * @param rx 长半轴
     * @param ry 短半轴
     * @param large_arc_flag 1 表示大角度弧线 0 表示小角度弧线
     * @param sweep_flag 1 表示从起点到终点顺时针方向 0 表示从起点到终点逆时针方向
     * @param isInLine 是否内嵌
     * @throws Exception
     */
    public static String arcToPolyline(BigDecimal startXPre, BigDecimal startYPre, BigDecimal x1, BigDecimal y1, BigDecimal x2,
                                       BigDecimal y2, BigDecimal endXPost, BigDecimal endYPost, BigDecimal rx, BigDecimal ry,
                                       BigDecimal large_arc_flag, BigDecimal sweep_flag,boolean isInLine) throws Exception {
        //是否内嵌
//        boolean isInLine = true;
        //x1 y1 rx ry xA la sf x2 y2
//        BigDecimal startXPre = new BigDecimal(150);
//        BigDecimal startYPre = new BigDecimal(120);

//        BigDecimal x1 = new BigDecimal(125);
//        BigDecimal y1 = new BigDecimal(75);

//        BigDecimal x2 = new BigDecimal(125);
//        BigDecimal y2 = new BigDecimal(0);

//        BigDecimal endXPost = new BigDecimal(80);
//        BigDecimal endYPost = new BigDecimal(-40);

//        BigDecimal rx = new BigDecimal(100);
//        BigDecimal ry = new BigDecimal(50);

        //旋转角度，不提供此属性
        BigDecimal x_axis_rotation = BigDecimal.ZERO;
//        BigDecimal large_arc_flag = BigDecimal.ZERO;
//        BigDecimal sweep_flag = BigDecimal.ZERO;

        SvgArcObject svgArcObject = SvgArcUtil.svgArcToCenterParam(x1, y1, rx, ry, x_axis_rotation, large_arc_flag,
                sweep_flag, x2, y2);
        Integer startMulti = Integer.valueOf(svgArcObject.getStartDegrees().intValue()/360);
        Integer endMulti = Integer.valueOf(svgArcObject.getEndDegrees().intValue()/360);
        if(UtilNumber.lessThan(svgArcObject.getStartDegrees(),BigDecimal.ZERO) && startMulti == 0){
            startMulti = -1;
        }
        if(UtilNumber.lessThan(svgArcObject.getEndDegrees(),BigDecimal.ZERO) && startMulti == 0){
            endMulti = -1;
        }
        //起始角度
        BigDecimal startDegrees = UtilNumber.numberAdd(svgArcObject.getStartDegrees(),UtilNumber.numberMultiply(new BigDecimal(360),new BigDecimal(-startMulti)));
        //结束角度
        BigDecimal endDegrees = UtilNumber.numberAdd(svgArcObject.getEndDegrees(),UtilNumber.numberMultiply(new BigDecimal(360),new BigDecimal(-endMulti)));

        String result = "";
        if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
            //逆时针
            int startQuadrant = getQuadrant(startDegrees);
            int endQuadrant = getQuadrant(endDegrees);
            int dValue = endQuadrant - startQuadrant;
            if(dValue == 0){
                if(UtilNumber.lessEqualThan(endDegrees,startDegrees)){
                    if(isInLine){
                        //向内
                        result = setPointStr(x1,y1);
                        result +=setPointStr(x2,y2);
                        System.out.println(result);
                    }else{
                        //向外
                        result = getOutPoint(startQuadrant, rx, ry, svgArcObject.getCx(), svgArcObject.getCy(),
                                startQuadrant, x1, y1, endQuadrant, x2, y2, sweep_flag, startXPre, startYPre, endXPost,
                                endYPost,false);
                        result += setPointStr(x2,y2);
                        System.out.println(result);
                    }
                }else{
                    dValue = 4;
                }
            }else if(dValue < 0){
                dValue = 4+dValue;
            }
            if(isInLine){
                //向内
                result = setPointStr(x1,y1);
                for(int index = 0 ;index < dValue; index++){
                    int nowQuadrant = startQuadrant + index;
                    result += getPoint(nowQuadrant,rx,ry,svgArcObject.getCx(),svgArcObject.getCy())+" ";
                }
                result += setPointStr(x2,y2);
                System.out.println(result);
            }else {
                //向外
                for(int index = 0 ;index <= dValue; index++){
                    int nowQuadrant = startQuadrant + index;
                    result += getOutPoint(nowQuadrant, rx, ry, svgArcObject.getCx(), svgArcObject.getCy(),
                            startQuadrant, x1, y1, endQuadrant, x2, y2, sweep_flag, startXPre, startYPre, endXPost,
                            endYPost,true);
                }
                System.out.println(result);
            }
        } else if(UtilNumber.equals(sweep_flag,BigDecimal.ONE)){
            //顺时针
            int startQuadrant = getQuadrant(startDegrees);
            int endQuadrant = getQuadrant(endDegrees);
            int dValue = endQuadrant - startQuadrant;
            if(dValue == 0){
                if(UtilNumber.greaterEqualThan(endDegrees,startDegrees)){
                    if(isInLine){
                        //向内
                        result = setPointStr(x1,y1);
                        result += setPointStr(x2,y2);
                        System.out.println(result);
                    }else {
                        //向外
                        result = getOutPoint(startQuadrant, rx, ry, svgArcObject.getCx(), svgArcObject.getCy(),
                                startQuadrant, x1, y1, endQuadrant, x2, y2, sweep_flag, startXPre, startYPre, endXPost,
                                endYPost,false);
                        result += setPointStr(x2,y2);
                        System.out.println(result);
                    }
                }else{
                    dValue = 4;
                }
            }else if(dValue < 0){
                dValue = -dValue;
            }
            if(isInLine){
                //向内
                result = setPointStr(x1,y1);
                for(int index = 1 ;index <= dValue; index++){
                    int nowQuadrant = startQuadrant - index;
                    result += getPoint(nowQuadrant,rx,ry,svgArcObject.getCx(),svgArcObject.getCy())+" ";
                }
                result += setPointStr(x2,y2);
                System.out.println(result);
            }else {
                //向外
                for(int index = 0 ;index <= dValue; index++){
                    int nowQuadrant = startQuadrant - index;
                    result += getOutPoint(nowQuadrant, rx, ry, svgArcObject.getCx(), svgArcObject.getCy(),
                            startQuadrant, x1, y1, endQuadrant, x2, y2, sweep_flag, startXPre, startYPre, endXPost,
                            endYPost,true);
                }
                System.out.println(result);
            }
        }
        return result;
    }

    /**
     * 获取角度所在的象限
     * @param degrees
     * @return
     * @throws Exception
     */
    public static int getQuadrant(BigDecimal degrees) throws Exception {
        if(UtilNumber.greaterThan(degrees,BigDecimal.ZERO) && UtilNumber.lessEqualThan(degrees,new BigDecimal(90))){
            return 1;
        }else if(UtilNumber.greaterThan(degrees,new BigDecimal(90)) && UtilNumber.lessEqualThan(degrees,new BigDecimal(180))){
            return 2;
        }else if(UtilNumber.greaterThan(degrees,new BigDecimal(180)) && UtilNumber.lessEqualThan(degrees,new BigDecimal(270))){
            return 3;
        }else if(UtilNumber.greaterThan(degrees,new BigDecimal(270)) && UtilNumber.lessEqualThan(degrees,new BigDecimal(360))){
            return 4;
        }else if(UtilNumber.equals(degrees,BigDecimal.ZERO)){
            return 4;
        }else {
            throw new Exception("Degrees set Error");
        }
    }

    /**
     * 获取象限所在内凹点坐标
     * @param quadrant
     * @param rx 长半轴
     * @param ry 短半轴
     * @param cx 焦点x坐标
     * @param cy 焦点y坐标
     * @return
     * @throws Exception
     */
    public static String getPoint(int quadrant,BigDecimal rx,BigDecimal ry,BigDecimal cx,BigDecimal cy)
            throws Exception {
        if(quadrant > 4){
            quadrant = quadrant%4;
        }
        if(quadrant == 1){
            return setPointStr(cx,UtilNumber.numberSubstract(cy,ry));
        }else if(quadrant == 2){
            return setPointStr(UtilNumber.numberSubstract(cx,rx),cy);
        }else if(quadrant == 3){
            return setPointStr(cx,UtilNumber.numberAdd(cy,ry));
        }else if(quadrant == 4 || quadrant == 0){
            return setPointStr(UtilNumber.numberAdd(cx,rx),cy);
        }else {
            throw new Exception("Quadrant set Error");
        }
    }

    /**
     * 获取象限所在外凸点的坐标
     * @param quadrant
     * @param rx
     * @param ry
     * @param cx
     * @param cy
     * @param startQuadrant
     * @param x1
     * @param y1
     * @param endQuadrant
     * @param x2
     * @param y2
     * @param sweep_flag
     * @param startXPre
     * @param startYPre
     * @param endXPost
     * @param endYPost
     * @param isNeedFocus  是否需要坐标轴上的点
     * @return
     * @throws Exception
     */
    public static String getOutPoint(int quadrant, BigDecimal rx, BigDecimal ry, BigDecimal cx, BigDecimal cy,
                                     int startQuadrant, BigDecimal x1, BigDecimal y1, int endQuadrant, BigDecimal x2, BigDecimal y2,
                                     BigDecimal sweep_flag, BigDecimal startXPre, BigDecimal startYPre, BigDecimal endXPost, BigDecimal endYPost,boolean isNeedFocus)
            throws Exception {
        String result = "";
        if (quadrant > 4) {
            quadrant = quadrant % 4;
        }
        if (quadrant == 1) {
            if(quadrant == startQuadrant){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    //逆时针
                    if (isInCircle(cx, cy, rx, ry, startXPre, startYPre) || UtilNumber.greaterThan(startYPre, y1)
                            || UtilNumber.greaterThan(startXPre, x1)) {
                        result += setPointStr(x1,y1);
                    }
                    result += setPointStr(x1 ,UtilNumber.numberSubstract(cy, ry));
                }else{
                    if (isInCircle(cx, cy, rx, ry, startXPre, startYPre) || UtilNumber.lessEqualThan(startXPre, x1)
                            || UtilNumber.lessThan(startYPre, y1)) {
                        result += setPointStr(x1,y1);
                    }
                    result += setPointStr(UtilNumber.numberAdd(cx, rx), y1 );
                }
            }else if(quadrant == endQuadrant){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    //逆时针
                    result += setPointStr(UtilNumber.numberAdd(cx, rx), y2);
                    if (isInCircle(cx, cy, rx, ry, endXPost, endYPost) || UtilNumber.lessEqualThan(endXPost, x2)
                            || UtilNumber.lessThan(endYPost, y2)) {
                        result += setPointStr(x2,y2);
                    }
                }else {
                    result += setPointStr(x2, UtilNumber.numberSubstract(cy, ry));
                    if (isInCircle(cx, cy, rx, ry, endXPost, endYPost) || UtilNumber.greaterThan(endYPost, y2)
                            || UtilNumber.greaterThan(endXPost, x2)) {
                        result += setPointStr(x2,y2);
                    }
                }
                isNeedFocus = false;
            }else{
                result += setPointStr(UtilNumber.numberAdd(cx, rx),UtilNumber.numberSubstract(cy, ry));
            }
            if(isNeedFocus){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    result += setPointStr(cx,UtilNumber.numberSubstract(cy, ry));
                }else {
                    result += setPointStr(UtilNumber.numberAdd(cx, rx),cy);
                }
            }
        } else if (quadrant == 2) {
            if(quadrant == startQuadrant){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    //逆时针
                    if (isInCircle(cx, cy, rx, ry, startXPre, startYPre) || UtilNumber.greaterThan(startXPre, x1)
                            || UtilNumber.greaterThan(startYPre, y1)) {
                        result += setPointStr(x1,y1);
                    }
                    result += setPointStr(UtilNumber.numberSubstract(cx, rx),y1);
                }else{
                    if (isInCircle(cx, cy, rx, ry, startXPre, startYPre) || UtilNumber.lessEqualThan(startYPre, y1)
                            || UtilNumber.lessThan(startXPre, x1)) {
                        result += setPointStr(x1,y1);
                    }
                    result += setPointStr(x1,UtilNumber.numberSubstract(cy, ry));
                }
            }else if(quadrant == endQuadrant){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    //逆时针
                    result += setPointStr(x2,UtilNumber.numberSubstract(cy, ry));
                    if (isInCircle(cx, cy, rx, ry, endXPost, endYPost) || UtilNumber.lessEqualThan(endYPost, y2)
                            || UtilNumber.lessThan(endXPost, x2)) {
                        result += setPointStr(x2,y2);
                    }
                }else{
                    result += setPointStr(UtilNumber.numberSubstract(cx, rx),y2);
                    if (isInCircle(cx, cy, rx, ry, endXPost, endYPost) || UtilNumber.greaterThan(endXPost, x2)
                            || UtilNumber.greaterThan(startYPre, y2)) {
                        result += setPointStr(x2,y2);
                    }
                }
                isNeedFocus = false;
            }else{
                result += setPointStr(UtilNumber.numberSubstract(cx, rx),UtilNumber.numberSubstract(cy, ry));
            }
            if(isNeedFocus){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    result += setPointStr(UtilNumber.numberSubstract(cx, rx),cy);
                }else {
                    result += setPointStr(cx,UtilNumber.numberSubstract(cy, ry));
                }
            }
        } else if (quadrant == 3) {
            if(quadrant == startQuadrant){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    if (isInCircle(cx, cy, rx, ry, startXPre, startYPre) || UtilNumber.lessEqualThan(startYPre, y1)
                            || UtilNumber.lessThan(startXPre, x1)) {
                        result += setPointStr(x1,y1);
                    }
                    //逆时针
                    result += setPointStr(x1,UtilNumber.numberAdd(cy,ry));
                }else{
                    if (isInCircle(cx, cy, rx, ry, startXPre, startYPre) || UtilNumber.greaterThan(startXPre, x1)
                            || UtilNumber.greaterEqualThan(startYPre, y1)) {
                        result += setPointStr(x1,y1);
                    }
                    result += setPointStr(UtilNumber.numberSubstract(cx,rx),y1);
                }
            }else if(quadrant == endQuadrant){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    //逆时针
                    result += setPointStr(UtilNumber.numberSubstract(cx,rx),y2);
                    if (isInCircle(cx, cy, rx, ry, endXPost, endYPost) || UtilNumber.greaterThan(endXPost, x2)
                            || UtilNumber.greaterEqualThan(endYPost, y2)) {
                        result += setPointStr(x2,y2);
                    }
                }else{
                    result += setPointStr(x2,UtilNumber.numberAdd(cy,ry));
                    if (isInCircle(cx, cy, rx, ry, endXPost, endYPost) || UtilNumber.lessEqualThan(endYPost, y2)
                            || UtilNumber.lessThan(endXPost, x2)) {
                        result += setPointStr(x2,y2);
                    }
                }
                isNeedFocus = false;
            }else {
                result += setPointStr(UtilNumber.numberSubstract(cx, rx),UtilNumber.numberAdd(cy, ry));
            }
            if (isNeedFocus){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    result += setPointStr(cx,UtilNumber.numberAdd(cy, ry));
                }else {
                    result += setPointStr(UtilNumber.numberSubstract(cx, rx),cy);
                }
            }
        } else if (quadrant == 4 || quadrant == 0) {
            if(quadrant == startQuadrant){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    if (isInCircle(cx, cy, rx, ry, startXPre, startYPre) || UtilNumber.lessEqualThan(startXPre, x1)
                            || UtilNumber.greaterThan(startYPre, y1)) {
                        result += setPointStr(x1,y1);
                    }
                    //逆时针
                    result += setPointStr(UtilNumber.numberAdd(cx,rx),y1);
                }else{
                    if (isInCircle(cx, cy, rx, ry, startXPre, startYPre) || UtilNumber.lessEqualThan(startYPre, y1)
                            || UtilNumber.greaterThan(startXPre, x1)) {
                        result += setPointStr(x1,y1);
                    }
                    result += setPointStr(x1, UtilNumber.numberAdd(cy,ry));
                }
            }else if(quadrant == endQuadrant){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    //逆时针
                    result += setPointStr(x2,UtilNumber.numberAdd(cy,ry));
                    if (isInCircle(cx, cy, rx, ry, endXPost, endYPost) || UtilNumber.lessEqualThan(endYPost, y2)
                            || UtilNumber.greaterThan(endXPost, x2)) {
                        result += setPointStr(x2,y2);
                    }
                }else{
                    result += setPointStr(UtilNumber.numberAdd(cx,rx),y2);
                    if (isInCircle(cx, cy, rx, ry, endXPost, endYPost) || UtilNumber.lessEqualThan(endXPost, x2)
                            || UtilNumber.greaterThan(endYPost, y2)) {
                        result += setPointStr(x2,y2);
                    }
                }
                isNeedFocus = false;
            }else {
                result += setPointStr(UtilNumber.numberAdd(cx, rx),UtilNumber.numberAdd(cy, ry));
            }
            if (isNeedFocus){
                if(UtilNumber.equals(sweep_flag,BigDecimal.ZERO)){
                    result += setPointStr(UtilNumber.numberAdd(cx, rx),cy);
                }else{
                    result += setPointStr(cx,UtilNumber.numberAdd(cy, ry));
                }
            }
        } else {
            throw new Exception("Quadrant set Error");
        }
        return result;
    }

    /**
     * 判断点是否在椭圆内
     * （x-x1）^2/A^2 + (y-y1)^2/B^2 < 1 在椭圆内
     * （x-x1）^2/A^2 + (y-y1)^2/B^2 = 1 在椭圆上
     * （x-x1）^2/A^2 + (y-y1)^2/B^2 > 1 在椭圆外
     *
     * @param cx 焦点
     * @param cy 焦点
     * @param rx 长半轴
     * @param ry 短半轴
     * @return
     */
    public static boolean isInCircle(BigDecimal cx, BigDecimal cy, BigDecimal rx, BigDecimal ry, BigDecimal x,
                                     BigDecimal y) {
        boolean isInCircle = true;
//        BigDecimal tmpX = UtilNumber.numberSubstract(cx, x);
//        BigDecimal tmpY = UtilNumber.numberSubstract(cy, y);
        BigDecimal tmpX = UtilNumber.numberSubstract(x, cx);
        BigDecimal tmpY = UtilNumber.numberSubstract(y, cy);
        BigDecimal result = UtilNumber.numberAdd(
                UtilNumber.numberDivideZero(UtilNumber.numberMultiply(tmpX, tmpX), UtilNumber.numberMultiply(rx, rx)),
                UtilNumber.numberDivideZero(UtilNumber.numberMultiply(tmpY, tmpY), UtilNumber.numberMultiply(ry, ry)));
        if (UtilNumber.greaterThan(result, BigDecimal.ONE)) {
            isInCircle = false;
        }
        return isInCircle;
    }

    public static String setPointStr(BigDecimal x,BigDecimal y){
        return String.format("%s,%s ",x.stripTrailingZeros(),y.stripTrailingZeros());
//        return String.format("L %s %s ",x,y);
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
