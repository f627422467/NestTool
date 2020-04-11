package com.aries.util.nest.util;

import com.aries.util.nest.data.Bezier;
import com.aries.util.nest.data.SVGPoint;
import com.aries.util.nest.data.SvgArcObject;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;

/**
 * svg 计算
 */
public class SvgArcUtil {

    private static double TAU = Math.PI * 2;


    public static BigDecimal radian(BigDecimal ux, BigDecimal uy, BigDecimal vx, BigDecimal vy) {
        BigDecimal dot = UtilNumber.numberAdd(UtilNumber.numberMultiply(ux, vx), UtilNumber.numberMultiply(uy, vy));
        double mod = Math.sqrt(UtilNumber
            .numberMultiply(UtilNumber.numberAdd(UtilNumber.numberMultiply(ux, ux), UtilNumber.numberMultiply(uy, uy)),
                UtilNumber.numberAdd(UtilNumber.numberMultiply(vx, vx), UtilNumber.numberMultiply(vy, vy)))
            .doubleValue());
        double rad = Math.acos(UtilNumber.numberDivideZero(dot, new BigDecimal(mod)).doubleValue());
        if (UtilNumber.lessThan(
            UtilNumber.numberSubstract(UtilNumber.numberMultiply(ux, vy), UtilNumber.numberMultiply(uy, vx)),
            BigDecimal.ZERO)) {
            rad = -rad;
        }
        return new BigDecimal(rad);
    }

    /**
     * 根据svg弧线路径参数计算中心点坐标、起始坐标、起始角度、结束角度
     * @param x1 起点x
     * @param y1 起点y
     * @param rx 长半轴
     * @param ry 短半轴
     * @param phi 旋转角度 (只能为0，如果有旋转角度，则计算出来的不准确)
     * @param fA 大小弧
     * @param fS 是否顺时针
     * @param x2 终点x
     * @param y2 终点y
     * @return
     * @throws Exception
     */
    public static SvgArcObject svgArcToCenterParam(BigDecimal x1, BigDecimal y1, BigDecimal rx, BigDecimal ry,
                                                   BigDecimal phi, BigDecimal fA, BigDecimal fS, BigDecimal x2, BigDecimal y2) throws Exception {
        BigDecimal PIx2 = UtilNumber.numberMultiply(new BigDecimal(2), new BigDecimal(Math.PI));
        if (UtilNumber.lessThan(rx, BigDecimal.ZERO)) {
            rx = rx.negate();
        }
        if (UtilNumber.lessThan(ry, BigDecimal.ZERO)) {
            ry = ry.negate();
        }
        if (UtilNumber.equals(rx, BigDecimal.ZERO) || UtilNumber.equals(ry, BigDecimal.ZERO)) {
            throw new Exception("rx or ry can not be Zero");
        }
        double s_phi = Math.sin(phi.doubleValue());
        double c_phi = Math.cos(phi.doubleValue());

        BigDecimal hd_x = UtilNumber.numberDivideZero(UtilNumber.numberSubstract(x1, x2), new BigDecimal(2));
        BigDecimal hd_y = UtilNumber.numberDivideZero(UtilNumber.numberSubstract(y1, y2), new BigDecimal(2));
        BigDecimal hs_x = UtilNumber.numberDivideZero(UtilNumber.numberAdd(x1, x2), new BigDecimal(2));
        BigDecimal hs_y = UtilNumber.numberDivideZero(UtilNumber.numberAdd(y1, y2), new BigDecimal(2));

        BigDecimal x1_ = UtilNumber.numberAdd(UtilNumber.numberMultiply(new BigDecimal(c_phi), hd_x),
            UtilNumber.numberMultiply(new BigDecimal(s_phi), hd_y));
        BigDecimal y1_ = UtilNumber.numberSubstract(UtilNumber.numberMultiply(new BigDecimal(c_phi), hd_y),
            UtilNumber.numberMultiply(new BigDecimal(s_phi), hd_x));

        BigDecimal lambda = UtilNumber.numberAdd(
            UtilNumber.numberDivideZero(UtilNumber.numberMultiply(x1_, x1_), UtilNumber.numberMultiply(rx, rx)),
            UtilNumber.numberDivideZero(UtilNumber.numberMultiply(y1_, y1_), UtilNumber.numberMultiply(ry, ry)));
        if (UtilNumber.greaterThan(lambda, BigDecimal.ONE)) {
            rx = UtilNumber.numberMultiply(rx, new BigDecimal(Math.sqrt(lambda.doubleValue())));
            ry = UtilNumber.numberMultiply(ry, new BigDecimal(Math.sqrt(lambda.doubleValue())));
        }

        BigDecimal rxry = UtilNumber.numberMultiply(rx, ry);
        BigDecimal rxy1_ = UtilNumber.numberMultiply(rx, y1_);
        BigDecimal ryx1_ = UtilNumber.numberMultiply(ry, x1_);

        BigDecimal sum_of_sq = UtilNumber.numberAdd(UtilNumber.numberMultiply(rxy1_, rxy1_),
            UtilNumber.numberMultiply(ryx1_, ryx1_));
        if (UtilMisc.isNullOrZero(sum_of_sq)) {
            throw new Exception("Start Point can not be same as End Point");
        }
        double coe = Math.sqrt(Math.abs(UtilNumber
            .numberDivide(UtilNumber.numberSubstract(UtilNumber.numberMultiply(rxry, rxry), sum_of_sq), sum_of_sq)
            .doubleValue()));
        if (UtilNumber.equals(fA, fS)) {
            coe = -coe;
        }
        BigDecimal cx_ = UtilNumber.numberDivideZero(UtilNumber.numberMultiply(new BigDecimal(coe), rxy1_), ry);
        BigDecimal cy_ = UtilNumber.numberDivideZero(UtilNumber.numberMultiply(new BigDecimal(coe), ryx1_), rx);

        BigDecimal cy_1 = UtilNumber.numberDivideZero(UtilNumber.numberMultiply(new BigDecimal(coe).negate(), ryx1_), rx);

        BigDecimal cx = UtilNumber
            .numberAdd(UtilNumber.numberSubstract(UtilNumber.numberMultiply(new BigDecimal(c_phi), cx_),
                UtilNumber.numberMultiply(new BigDecimal(s_phi), cy_)), hs_x);
        BigDecimal cy = UtilNumber.numberAdd(UtilNumber.numberMultiply(new BigDecimal(s_phi), cx_),
            UtilNumber.numberMultiply(new BigDecimal(c_phi), cy_1), hs_y);

        BigDecimal xcr1 = UtilNumber.numberDivideZero(UtilNumber.numberSubstract(x1_, cx_), rx);
        BigDecimal xcr2 = UtilNumber.numberDivideZero(UtilNumber.numberAdd(x1_, cx_), rx);
        BigDecimal ycr1 = UtilNumber.numberDivideZero(UtilNumber.numberSubstract(y1_, cy_), ry);
        BigDecimal ycr2 = UtilNumber.numberDivideZero(UtilNumber.numberAdd(y1_, cy_), ry);

        BigDecimal startAngle = radian(BigDecimal.ONE, BigDecimal.ZERO, xcr1, ycr1);

        BigDecimal deltaAngle = radian(xcr1, ycr1, xcr2.negate(), ycr2.negate());

        while (UtilNumber.greaterThan(deltaAngle, PIx2)) {
            deltaAngle = UtilNumber.numberSubstract(deltaAngle, PIx2);
        }
        while (UtilNumber.lessThan(deltaAngle, BigDecimal.ZERO)) {
            deltaAngle = UtilNumber.numberAdd(deltaAngle, PIx2);
        }
        if (UtilNumber.equals(fS, BigDecimal.ZERO)) {
            deltaAngle = UtilNumber.numberSubstract(deltaAngle, PIx2);
        }

        BigDecimal endAngle = UtilNumber.numberAdd(startAngle, deltaAngle);

        while (UtilNumber.greaterThan(endAngle, PIx2)) {
            endAngle = UtilNumber.numberSubstract(endAngle, PIx2);
        }
        while (UtilNumber.lessThan(endAngle, BigDecimal.ZERO)) {
            endAngle = UtilNumber.numberAdd(endAngle, PIx2);
        }
        SvgArcObject svgArcObject = new SvgArcObject();
        svgArcObject.setCx(cx);
        svgArcObject.setCy(cy);
        svgArcObject.setStartAngle(startAngle);
        svgArcObject.setStartDegrees(new BigDecimal(Math.toDegrees(svgArcObject.getStartAngle().doubleValue())));
        svgArcObject.setDeltaAngle(deltaAngle);
        svgArcObject.setEndAngle(endAngle);
        svgArcObject.setEndDegrees(new BigDecimal(Math.toDegrees(svgArcObject.getEndAngle().doubleValue())));

        //顺时针时，是按照正常象限方向，逆时针时相反，故要用360°减
//        if(UtilNumber.equals(fS,BigDecimal.ZERO)){
            svgArcObject.setStartDegrees(UtilNumber.numberSubstract(new BigDecimal(360),svgArcObject.getStartDegrees()));
            svgArcObject.setEndDegrees(UtilNumber.numberSubstract(new BigDecimal(360),svgArcObject.getEndDegrees()));
//        }
        return svgArcObject;
    }

    /**
     * 将SVG的弧线转为 多个贝塞尔三次曲线
     *里面需要注意的是，如果有整数的除法，会默认成整数，
     *
     */
    public static List<Bezier> arcToBezier(double px, double py, double cx, double cy, double rx, double ry,
                                           BigDecimal xAxisRotation, BigDecimal largeArcFlag, BigDecimal sweepFlag) {
        if (rx == 0 || ry == 0) {
            return null;
        }
        double sinphi = Math.sin(xAxisRotation.doubleValue() * TAU / 360);
        double cosphi = Math.cos(xAxisRotation.doubleValue() * TAU / 360);
        double pxp = cosphi * (px - cx) / 2 + sinphi * (py - cy) / 2;
        double pyp = -sinphi * (px - cx) / 2 + cosphi * (py - cy) / 2;
        if (pxp == 0 && pyp == 0) {
            return null;
        }
        rx = Math.abs(rx);
        ry = Math.abs(ry);
        double lambda = Math.pow(pxp, 2) / Math.pow(rx, 2)
            + Math.pow(pyp, 2) / Math.pow(ry, 2);
        if (lambda > 1) {
            rx *= Math.sqrt(lambda);
            ry *= Math.sqrt(lambda);
        }
        SvgArcObject svgArcObject = getArcCenter(px, py, cx, cy, rx, ry, largeArcFlag, sweepFlag, sinphi, cosphi, pxp,
            pyp);
        double centerx = svgArcObject.getCx().doubleValue();
        double centery = svgArcObject.getCy().doubleValue();
        double ang1 = svgArcObject.getStartAngle().doubleValue();
        double ang2 = svgArcObject.getEndAngle().doubleValue();

        double ratio = Math.abs(ang2) / (TAU / 4);
        if (Math.abs(1.0 - ratio) < 0.0000001) {
            ratio = 1.0;
        }
        double segments = Math.max(Math.ceil(ratio), 1);
        ang2 /= segments;

        List<Bezier> beziers = Lists.newArrayList();
        for (double i = 0; i < segments; i++) {
            List<SVGPoint> points = approxUnitArc(ang1, ang2);
            Bezier bezier = new Bezier();
            SVGPoint point1 = mapToEllipse(points.get(0), rx, ry, cosphi, sinphi, centerx, centery);
            bezier.setX1(point1.getX());
            bezier.setY1(point1.getY());

            SVGPoint point2 = mapToEllipse(points.get(1), rx, ry, cosphi, sinphi, centerx, centery);
            bezier.setX2(point2.getX());
            bezier.setY2(point2.getY());

            SVGPoint point = mapToEllipse(points.get(2), rx, ry, cosphi, sinphi, centerx, centery);
            bezier.setX(point.getX());
            bezier.setY(point.getY());
            beziers.add(bezier);
            ang1 += ang2;
        }
        return beziers;
    }


    private static SvgArcObject getArcCenter(double px, double py, double cx, double cy, double rx, double ry, BigDecimal largeArcFlag,
        BigDecimal sweepFlag, double sinphi, double cosphi, double pxp, double pyp) {
        double rxsq = Math.pow(rx, 2);
        double rysq = Math.pow(ry, 2);
        double pxpsq = Math.pow(pxp, 2);
        double pypsq = Math.pow(pyp, 2);
        double radicant = (rxsq * rysq) - (rxsq * pypsq) - (rysq * pxpsq);
        if (radicant < 0) {
            radicant = 0;
        }
        radicant /= (rxsq * pypsq) + (rysq * pxpsq);
        radicant = Math.sqrt(radicant) * ( UtilNumber.equals(largeArcFlag,sweepFlag) ? -1 : 1);

        double centerxp = radicant * rx / ry * pyp;
        double centeryp = radicant * -ry / rx * pxp;

        double centerx = cosphi * centerxp - sinphi * centeryp + (px + cx) / 2;
        double centery = sinphi * centerxp + cosphi * centeryp + (py + cy) / 2;

        double vx1 = (pxp - centerxp) / rx;
        double vy1 = (pyp - centeryp) / ry;
        double vx2 = (-pxp - centerxp) / rx;
        double vy2 = (-pyp - centeryp) / ry;

        double ang1 = vectorAngle(1, 0, vx1, vy1);
        double ang2 = vectorAngle(vx1, vy1, vx2, vy2);

        if (UtilNumber.equals(sweepFlag,BigDecimal.ZERO) && ang2 > 0) {
            ang2 -= TAU;
        }

        if (UtilNumber.equals(sweepFlag,BigDecimal.ONE) && ang2 < 0) {
            ang2 += TAU;
        }
        SvgArcObject svgArcObject = new SvgArcObject();
        svgArcObject.setCx(new BigDecimal(centerx));
        svgArcObject.setCy(new BigDecimal(centery));
        svgArcObject.setStartAngle(new BigDecimal(ang1));
        svgArcObject.setEndAngle(new BigDecimal(ang2));
        return svgArcObject;
    }

    private static double vectorAngle(double ux, double uy, double vx, double vy){
        double sign = (ux * vy - uy * vx < 0) ? -1 : 1;
        double dot = ux * vx + uy * vy;
        if (dot > 1) {
            dot = 1;
        }
        if (dot < -1) {
            dot = -1;
        }
        return sign * Math.acos(dot);
    }

    private static List<SVGPoint> approxUnitArc(double ang1, double ang2) {
        double a = ang2 == 1.5707963267948966 ? 0.551915024494
            : ang2 == -1.5707963267948966 ? -0.551915024494
                : (UtilNumber.numberDivideZero(new BigDecimal(4), new BigDecimal(3)).doubleValue())
                    * Math.tan(ang2 / 4);

        double x1 = Math.cos(ang1);
        double y1 = Math.sin(ang1);
        double x2 = Math.cos(ang1 + ang2);
        double y2 = Math.sin(ang1 + ang2);

        List<SVGPoint> points = Lists.newArrayList();
        SVGPoint point1 = new SVGPoint();
        point1.setX(new BigDecimal(x1 - y1 * a));
        point1.setY(new BigDecimal(y1 + x1 * a));
        points.add(point1);

        SVGPoint point2 = new SVGPoint();
        point2.setX(new BigDecimal(x2 + y2 * a));
        point2.setY(new BigDecimal(y2 - x2 * a));
        points.add(point2);

        SVGPoint point3 = new SVGPoint();
        point3.setX(new BigDecimal(x2));
        point3.setY(new BigDecimal(y2));
        points.add(point3);

        return points;
    }

    private static SVGPoint mapToEllipse( SVGPoint point, double rx, double ry, double cosphi, double sinphi, double centerx, double centery){
        double x = point.getX().doubleValue();
        double y  = point.getY().doubleValue();
        x *= rx;
        y *= ry;
        double xp = cosphi * x - sinphi * y;
        double yp = sinphi * x + cosphi * y;

        SVGPoint result = new SVGPoint();
        result.setX(new BigDecimal(xp + centerx));
        result.setY(new BigDecimal(yp + centery));
        return result;
    }
}
