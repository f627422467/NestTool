package com.aries.util.nest;


import com.aries.util.nest.data.NestPath;
import com.aries.util.nest.data.Placement;
import com.aries.util.nest.data.*;
import com.aries.util.nest.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.*;


public class NestTest {
   

    @Test
    public void HoleTest() throws Exception {
        NestPath binPolygon = new NestPath();
        double width = 400;
        double height = 400;
        binPolygon.add(0, 0);
        binPolygon.add(0, height);
        binPolygon.add(width, height);
        binPolygon.add(width, 0);
        NestPath outer = new NestPath();
        outer.add(600, 0);
        outer.add(600, 200);
        outer.add(800, 200);
        outer.add(800, 0);
        outer.setRotation(0);
        outer.bid = 1;
        NestPath inner = new NestPath();
        inner.add(650, 50);
        inner.add(650, 150);
        inner.add(750, 150);
        inner.add(750, 50);
        inner.bid = 2;
        NestPath little = new NestPath();
        little.add(900, 0);
        little.add(870, 20);
        little.add(930, 20);
        little.bid = 3;
        little.setRotation(4);
        List<NestPath> list = new ArrayList<NestPath>();
        list.add(inner);
        list.add(outer);
        list.add(little);
        Config config = new Config();
        config.USE_HOLE = true;
        Nest nest = new Nest(binPolygon, list, config, 10);
        List<List<Placement>> appliedPlacement = nest.startNest();
        List<String> strings = SvgUtil.svgGenerator(list, appliedPlacement, width, height);
        for (String s : strings) {
            System.out.println(s);
        }
    }


    /**
     * 简单引用
     * @throws Exception
     */
    @Test
    public void testSample() throws Exception {
        List<NestPath> polygons = transferSvgIntoPolygons();
        NestPath bin = new NestPath();
        double binWidth = 511.822;
        double binHeight = 339.235;
        bin.add(0, 0);
        bin.add(binWidth, 0);
        bin.add(binWidth, binHeight);
        bin.add(0, binHeight);
        bin.bid = -1;
        Config config = new Config();
        config.SPACING = 0;
        config.POPULATION_SIZE = 5;
        Nest nest = new Nest(bin, polygons, config, 2);
        List<List<Placement>> appliedPlacement = nest.startNest();
        List<String> strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
        saveSvgFile(strings);
    }

    private List<NestPath> transferSvgIntoPolygons() throws DocumentException {
        List<NestPath> nestPaths = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read("test.xml");
        List<Element> elementList = document.getRootElement().elements();
        int count = 0;
        for (Element element : elementList) {
            count++;
            if ("polygon".equals(element.getName())) {
                String datalist = element.attribute("points").getValue();
                NestPath polygon = new NestPath();
                for (String s : datalist.split(" ")) {
                    s = s.trim();
                    if (s.indexOf(",") == -1) {
                        continue;
                    }
                    String[] value = s.split(",");
                    double x = Double.parseDouble(value[0]);
                    double y = Double.parseDouble(value[1]);
                    polygon.add(x, y);
                }
                polygon.bid = count;
                polygon.setRotation(4);
                nestPaths.add(polygon);
            } else if ("rect".equals(element.getName())) {
                double width = Double.parseDouble(element.attribute("width").getValue());
                double height = Double.parseDouble(element.attribute("height").getValue());
                double x = Double.parseDouble(element.attribute("x").getValue());
                double y = Double.parseDouble(element.attribute("y").getValue());
                NestPath rect = new NestPath();
                rect.add(x, y);
                rect.add(x + width, y);
                rect.add(x + width, y + height);
                rect.add(x, y + height);
                rect.bid = count;
                rect.setRotation(4);
                nestPaths.add(rect);
            }
        }
        return nestPaths;
    }

    private void saveSvgFile(List<String> strings) throws Exception {
        File f = new File("test.html");
        if (!f.exists()) {
            f.createNewFile();
        }
        Writer writer = new FileWriter(f, false);
        writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n" +
                "\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
                "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                " \n" +
                "<svg width=\"100%\" height=\"100%\" version=\"1.1\"\n" +
                "xmlns=\"http://www.w3.org/2000/svg\">\n");
        for(String s : strings){
            writer.write(s);
        }
        writer.write("</svg>");
        writer.close();
    }


    /**
     * 多svg同时套料：每个svg都有一套坐标系，必须把所有svg都转到同一坐标系
     *
     * 1. 将所有svg转到同一坐标系，此时必须不能有重叠的边
     * 2. 过程中，将圆、圆弧、多边形转化成点集
     * 3. 将所有点集录入到 NestPath
     * 4. 执行套料
     * 5. 将优化后的数据，写入svg文件
     * 6. 优化后的svg文件，输出nc设备加工文件
     *
     */
    @Test
    public void testNest(){
        List<File> svgFiles = Lists.newArrayList();
        svgFiles.add(new File("svgFile1Path"));
        svgFiles.add(new File("svgFile2Path"));
        svgFiles.add(new File("svgFile3Path"));

        //锯宽 ( 对于nc设备加工文件，需要先偏移锯宽后，再生成刀路)
        BigDecimal sawWidth = BigDecimal.ZERO;
        //左右部边部余量
        double sideLength = 0.0f;
        double binLength = 500f - sideLength;
        //上下部边部余量
        double sideHeight = 0.0f;
        double binHeight = 500f - sideHeight;
        //中心板件
        NestPath bin = new NestPath();
        bin.add(0, 0);
        bin.add(binLength, 0);
        bin.add(binLength, binHeight);
        bin.add(0, binHeight);
        bin.bid = -1;
        //构建所有svg所在的同一坐标系
        //虚拟坐标起点
        BigDecimal virtualX = UtilNumber.numberAdd(new BigDecimal(binLength), BigDecimal.TEN);
        //虚拟坐标起点
        BigDecimal virtualY = UtilNumber.numberAdd(new BigDecimal(binHeight), BigDecimal.TEN);
        //虚拟x轴最大值
        BigDecimal maxX = new BigDecimal(Integer.MIN_VALUE);
        //虚拟y轴最大值
        BigDecimal maxY = new BigDecimal(Integer.MIN_VALUE);
        //初始化待优化的零件集合，将其转化为算法内的对象
        List<NestPath> polygons = Lists.newArrayList();
        int count = 0;
        Map<String, Object> param = Maps.newHashMap();
        param.put("count", count);
        //遍历所有文件，将svg构建到同一坐标系中
        for (File svgFile : svgFiles) {
            polygons.addAll(transferSvgIntoPolygons(svgFile, virtualX, virtualY, param,
                    UtilNumber.numberDivide(sawWidth, new BigDecimal(2))));
            maxX = new BigDecimal(param.get("maxX").toString());
            maxY = new BigDecimal(param.get("maxY").toString());
            virtualX = UtilNumber.numberAdd(maxX, UtilNumber.max(new BigDecimal(binLength), new BigDecimal(binHeight)),
                    BigDecimal.TEN);
            if (UtilNumber.greaterThan(virtualX, new BigDecimal(100000))) {
                virtualY = UtilNumber.numberAdd(maxY,
                        UtilNumber.max(new BigDecimal(binLength), new BigDecimal(binHeight)), BigDecimal.TEN);
                virtualX = UtilNumber.numberAdd(new BigDecimal(binLength), BigDecimal.TEN);
                maxX = new BigDecimal(Integer.MIN_VALUE);
                maxY = new BigDecimal(Integer.MIN_VALUE);
            }
        }
        Config config = new Config();
        boolean isArc = false;
        for (NestPath nestPath : polygons) {
            if (nestPath.getIsIncludeArc()) {
                isArc = true;
                break;
            }
        }
        if (isArc) {
            config.CURVE_TOLERANCE = 0.04;
        }

        List<String> svgStr = SvgUtil.beforSvgNest(polygons, binLength, binHeight);
        try {
            //将所有svg转为同一坐标系后，打出来看看
            saveSvgFile(svgStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Nest nest = new Nest(bin, polygons, config, 5);
        List<List<Placement>> appliedPlacement = nest.startNest();
        List<File> afterSVGFiles = Lists.newArrayList();
        //将优化结果输出到svg文件中
        for (List<Placement> binlist : appliedPlacement) {
            File file = createNestSvg(polygons, binlist, binLength, binHeight,
                    sideLength, sideLength);
            afterSVGFiles.add(file);
        }
        //根据svg文件，输出nc设备加工文件
        //nc文件输出位置，由inkscape决定
        for (File svgFile : svgFiles) {
            String commondStr = String.format("c:/inkscape.exe"
                    + " --file=%s --verb EditSelectAll --verb SelectionOffset"
                    + " --verb ru.cnc-club.filter.gcodetools_orientation_no_options_no_preferences.noprefs"
                    + " --verb ru.cnc-club.filter.gcodetools_tools_library_no_options_no_preferences.noprefs"
                    + " --verb ru.cnc-club.filter.gcodetools_ptg.noprefs --verb=FileSave --verb=FileQuit", svgFile.getPath());
            System.out.println("commondStr:" + commondStr);
            String[] cmds = new String[]{"/bin/sh", "-c",
                    "export DISPLAY=:0" + "&&" + commondStr};
            System.out.println("cmds:" + cmds.toString());
            try {
                String osName = System.getProperty("os.name");
                if(osName.toLowerCase().contains("windows")){
                    Process p = Runtime.getRuntime().exec(commondStr);
                    UtilMisc.doWaitFor(p);
                }else{
                    Process p = Runtime.getRuntime().exec(cmds);
                    UtilMisc.doWaitFor(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public File createNestSvg(List<NestPath> list, List<Placement> binlist, double binwidth,
                              double binHeight, double sideLength, double sideWidth) {
        String svgTemplatePath = "C:/svgTemplate";
        String filePath = svgTemplatePath + File.separator + "Nest.svg";
        File document = new File(filePath);
        String xml = null;
        String exportPath = String.format("C:/svg_cache" + File.separator + "%s.svg",
                UUID.randomUUID());
        try {
            xml = UtilFile.readFile(document);
            Element svgRoot = DocumentHelper.parseText(xml).getRootElement();
            svgRoot.addAttribute("width",
                    new BigDecimal(binwidth + 2 * sideLength).stripTrailingZeros().toPlainString());
            svgRoot.addAttribute("height",
                    new BigDecimal(binHeight + 2 * sideWidth).stripTrailingZeros().toPlainString());
            Element background = SVGUtils.findObjectByID(svgRoot, "layer1");
            handleNestSVG(background, binlist, list);
            OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
            xmlFormat.setEncoding("utf-8");
            XMLWriter writer = new XMLWriter(new FileOutputStream(exportPath), xmlFormat);
            writer.write(svgRoot);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(exportPath);
    }

    //处理内容部分
    private void handleNestSVG(Element background, List<Placement> binlist, List<NestPath> list) {
        double x = 0;
        double y = 0;
        for (Placement placement : binlist) {
            int bid = placement.bid;
            NestPath nestPath = SvgUtil.getNestPathByBid(bid, list);
            double ox = placement.translate.x;
            double oy = placement.translate.y;
            double rotate = placement.rotate;
            handleElement(background, nestPath, ox, oy, rotate, x, y);
        }
    }

    //处理Element
    private void handleElement(Element parent, NestPath nestPath, double ox, double oy,
                               double rotate, double x, double y) {
        if (nestPath.getIsInline()) {
            //对于给NC的图中，不需要孔槽
            return;
        }
        Element path = parent.addElement("path");
        String d = "";
        BigDecimal maxX = new BigDecimal(Integer.MIN_VALUE);
        BigDecimal maxY = new BigDecimal(Integer.MIN_VALUE);
        BigDecimal minX = new BigDecimal(Integer.MAX_VALUE);
        BigDecimal minY = new BigDecimal(Integer.MAX_VALUE);
        for (int i = 0; i < nestPath.getSegments().size(); i++) {
            if (i == 0) {
                d += "M";
            } else {
                d += "L";
            }
            Segment segment = nestPath.get(i);
            d += segment.x + " " + segment.y + " ";

            //获取最大，最小值
            BigDecimal xB = new BigDecimal(segment.x);
            BigDecimal yB = new BigDecimal(segment.y);
            if (UtilNumber.greaterThan(xB, maxX)) {
                maxX = xB;
            }
            if (UtilNumber.greaterThan(yB, maxY)) {
                maxY = yB;
            }
            if (UtilNumber.lessThan(xB, minX)) {
                minX = xB;
            }
            if (UtilNumber.lessThan(yB, minY)) {
                minY = yB;
            }
        }
        d += "Z";
        path.addAttribute("d", d);
        setPublicAttribute(path, ox, oy, rotate, x, y, nestPath.getIsInline());
    }

    //设置共用属性
    private void setPublicAttribute(Element element, double ox, double oy, double rotate, double x, double y,
                                    boolean isInLine) {
        element.addAttribute("transform", String.format("translate(%s %s) rotate(%s)", (ox + x), (oy + y), rotate));
        element.addAttribute("fill", "none");
        if (isInLine) {
            element.addAttribute("stroke", "#ff0000");
        } else {
            element.addAttribute("stroke", "#010101");
        }
        element.addAttribute("stroke-width", "1");
    }



    /**
     * 将svg文件转NestPath。弧线、圆转为点集，并向外偏移半个锯宽
     * @param svgFile
     * @param virtualX
     * @param virtualY
     * @param param
     * @param sawWidth
     * @return
     */
    public List<NestPath> transferSvgIntoPolygons(File svgFile, BigDecimal virtualX, BigDecimal virtualY,
                                                  Map<String, Object> param, BigDecimal sawWidth) {
        List<NestPath> nestPaths = new ArrayList<>();
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(svgFile.getPath());
            Element svgRoot = document.getRootElement();
            Element background = SvgUtil.findObjectByID(svgRoot, "layer1");
            List<Element> elementList = background.elements();
            int count = Integer.valueOf(param.get("count").toString());
            for (Element element : elementList) {
                boolean isInLine = false;
                if (UtilMisc.isNotNull(element) && UtilMisc.isNotNull(element.attribute("isInLine"))
                        && UtilMisc.isNotEmpty(element.attribute("isInLine").getValue())) {
                    if (element.attribute("isInLine").getValue().equals("1")) {
                        isInLine = true;
                    }
                }
                count++;
                if ("rect".equals(element.getName())) {
                    double width = Double.parseDouble(element.attribute("width").getValue());
                    double height = Double.parseDouble(element.attribute("height").getValue());
                    double x = Double.parseDouble(element.attribute("x").getValue());
                    double y = Double.parseDouble(element.attribute("y").getValue());

                    if(isInLine){
                        width = width - UtilNumber.numberMultiply(new BigDecimal(2),sawWidth).doubleValue();
                        height = height - UtilNumber.numberMultiply(new BigDecimal(2),sawWidth).doubleValue();
                        x = x + sawWidth.doubleValue();
                        y = y + sawWidth.doubleValue();
                    }else{
                        width = width + UtilNumber.numberMultiply(new BigDecimal(2),sawWidth).doubleValue();
                        height = height + UtilNumber.numberMultiply(new BigDecimal(2),sawWidth).doubleValue();
                        x = x - sawWidth.doubleValue();
                        y = y - sawWidth.doubleValue();
                    }

                    NestPath rect = new NestPath();
                    if (UtilMisc.isNotNull(element.attribute("rx"))
                            && UtilMisc.isNotEmpty(element.attribute("rx").getValue())) {
                        //圆角、椭圆角矩形
                        double rx = Double.parseDouble(element.attribute("rx").getValue());
                        double ry = Double.parseDouble(element.attribute("ry").getValue());
                        if (isInLine) {
                            rx = rx - sawWidth.doubleValue();
                            ry = ry - sawWidth.doubleValue();
                        } else {
                            rx = rx + sawWidth.doubleValue();
                            ry = ry + sawWidth.doubleValue();
                        }
                        String points = "";
                        points += SvgUtil.ellipseToPoints(x+rx,y+ry,rx,ry,180,270);
                        points += SvgUtil.ellipseToPoints(x+width-rx,y+ry,rx,ry,270,360);
                        points += SvgUtil.ellipseToPoints(x+width-rx,y+height-ry,rx,ry,0,90);
                        points += SvgUtil.ellipseToPoints(x+rx,y+height-ry,rx,ry,90,180);
                        for (String s : points.split(" ")) {
                            s = s.trim();
                            if (s.indexOf(",") == -1) {
                                continue;
                            }
                            String[] value = s.split(",");
                            double tmpX = Double.parseDouble(value[0]);
                            double tmpY = Double.parseDouble(value[1]);
                            rect.addTrue(tmpX, tmpY);
                        }
                        rect.setIsIncludeArc(true);
                    } else {
                        rect.addTrue(x, y);
                        rect.addTrue(x + width, y);
                        rect.addTrue(x + width, y + height);
                        rect.addTrue(x, y + height);
                    }
                    rect.bid = count;
                    rect.setRotation(4);
                    rect.setIsInline(isInLine);
                    nestPaths.add(rect);
                } else if ("circle".equals(element.getName())) {
                    double cx = Double.parseDouble(element.attribute("cx").getValue());
                    double cy = Double.parseDouble(element.attribute("cy").getValue());
                    double r = Double.parseDouble(element.attribute("r").getValue());
                    if (isInLine) {
                        r = r - sawWidth.doubleValue();
                    } else {
                        r = r + sawWidth.doubleValue();
                    }
                    NestPath circle = new NestPath();
                    String points = SvgUtil.circleToPoints(cx,cy,r);
                    for (String s : points.split(" ")) {
                        s = s.trim();
                        if (s.indexOf(",") == -1) {
                            continue;
                        }
                        String[] value = s.split(",");
                        double x = Double.parseDouble(value[0]);
                        double y = Double.parseDouble(value[1]);
                        circle.addTrue(x, y);
                    }
                    circle.bid = count;
                    circle.setRotation(4);
                    circle.setIsInline(isInLine);
                    circle.setIsIncludeArc(true);
                    nestPaths.add(circle);
                }else if("path".equals(element.getName()) || "polygon".equals(element.getName())){
                    NestPath path = new NestPath();
                    List<BigDecimal> pointX  = Lists.newArrayList();
                    List<BigDecimal> pointY  = Lists.newArrayList();
                    List<Point> points = Lists.newArrayList();
                    boolean isIncludeArc = false;
                    if("polygon".equals(element.getName())){
                        String datalist = element.attribute("points").getValue();
                        for (String s : datalist.split(" ")) {
                            s = s.trim();
                            if (s.indexOf(",") == -1) {
                                continue;
                            }
                            String[] value = s.split(",");
                            double x = Double.parseDouble(value[0]);
                            double y = Double.parseDouble(value[1]);
                            Point point = new Point();
                            point.setX(new BigDecimal(x));
                            point.setY(new BigDecimal(y));
                            points.add(point);
                        }
                    }else{
                        String datalist = element.attribute("bg").getValue();
                        String[] parts = datalist.split(";");
                        for(int index =0;index < parts.length;index++){
                            String part = parts[index];
                            if(part.indexOf("A") != -1){
                                if(index == 0){
                                    throw new Exception("弧线不能放在图形的第一个！");
                                }
                                isIncludeArc = true;
                                String pathStr = part;
                                boolean isArcInwards = false;
                                if (part.indexOf("|") != -1) {
                                    pathStr = part.split("\\|")[0];
                                    if (UtilMisc.isNotNull(part.split("\\|")[1]) && part.split("\\|")[1].equals("1")) {
                                        isArcInwards = true;
                                    }
                                }
                                String[] paramStrs = pathStr.split(" ");
                                BigDecimal rx = new BigDecimal(paramStrs[1]);
                                BigDecimal ry = new BigDecimal(paramStrs[2]);
                                BigDecimal xAxisRotation = new BigDecimal(paramStrs[3]);
                                BigDecimal large_arc_flag = new BigDecimal(paramStrs[4]);
                                BigDecimal sweep_flag = new BigDecimal(paramStrs[5]);
                                BigDecimal x2 = new BigDecimal(paramStrs[6].split(",")[0].trim());
                                BigDecimal y2 = new BigDecimal(paramStrs[6].split(",")[1].trim());
                                Point point = new Point();
                                point.setX(x2);
                                point.setY(y2);
                                point.setRx(rx);
                                point.setRy(ry);
                                point.setX_axis_rotation(xAxisRotation);
                                point.setLarge_arc_flag(large_arc_flag);
                                point.setSweep_flag(sweep_flag);
                                point.setIsArcInwards(isArcInwards);
                                points.add(point);
                            }else {
                                //折线
                                String[] sArray = part.split(" ");
                                for (int i = 0; i < sArray.length; i++) {
                                    String s = sArray[i].trim();
                                    if (s.indexOf(",") == -1) {
                                        continue;
                                    }
                                    String[] value = s.split(",");
                                    double x = Double.parseDouble(value[0]);
                                    double y = Double.parseDouble(value[1]);
                                    Point point = new Point();
                                    point.setX(new BigDecimal(x));
                                    point.setY(new BigDecimal(y));
                                    points.add(point);
                                }
                            }
                        }
                    }

                    //根据point计算偏移半个刀路后的point
                    List<Line> lines = Lists.newArrayList();
                    for (int index = 1; index < points.size(); index++) {
                        Point startPoint = points.get(index - 1);
                        Point endPoint = points.get(index);
                        Line line = new Line();
                        lines.add(line);
                        line.setStartPoint(startPoint);
                        line.setEndPoint(endPoint);
                        if(UtilMisc.isNotNull(endPoint.getRx())){
                            line.setIsArc(true);
                            line.setRx(endPoint.getRx());
                            line.setRy(endPoint.getRy());
                            line.setX_axis_rotation(endPoint.getX_axis_rotation());
                            line.setLarge_arc_flag(endPoint.getLarge_arc_flag());
                            line.setSweep_flag(endPoint.getSweep_flag());
                            line.setIsArcInwards(endPoint.getIsArcInwards());
                        }
                    }

                    //去除重复
                    Iterator<Line> lineIterator = lines.iterator();
                    while (lineIterator.hasNext()){
                        Line line = lineIterator.next();
                        Point startPoint = line.getStartPoint();
                        Point endPoint = line.getEndPoint();
                        if(endPoint.equals(startPoint)){
                            lineIterator.remove();
                        }
                    }

                    //将曲线简单矩形化，目的是为了计算出，直线向外偏是向左还是向右还是向上还是向下。
                    //判断原理是，将直线右侧、上侧的一点，判断其是否在矩形化的图形内，若不在则是向该方向偏移，反之则是向相反的方向偏移
                    Polygon polygon = new Polygon();
                    polygon.setPoints(points);
                    polygon.reCompute();

                    for (int index = 0; index < lines.size(); index++) {
                        Line line1 = lines.get(index);
                        //先计算出line向外偏saw个距离的直线表达式
                        //TODO 目前没有内嵌多边形以及异形，故而这里暂不处理，向内偏移的情况，上面的圆，矩形已经处理了向内偏移
                        calculOffSetLine(line1,polygon,sawWidth);
                        Line line2 = null;
                        if(index + 1 < lines.size()){
                            line2 = lines.get(index+1);
                        }else{
                            line2 = lines.get(0);
                        }
                        calculOffSetLine(line2,polygon,sawWidth);
                        if(!line1.getIsArc() && !line2.getIsArc()){
                            //两条都是直线 计算出两条线的交点
                            Point focePoint = getLinePoint(line1,line2);
                            if(UtilMisc.isNotNull(focePoint)){
                                path.addTrue(focePoint.getX().doubleValue(), focePoint.getY().doubleValue());
                                pointX.add(focePoint.getX());
                                pointY.add(focePoint.getY());
                            }
                        }else if(!line1.getIsArc() && line2.getIsArc()){
                            //第一个是直线，第二个是曲线
                            Point focePoint = SVGUtils.getArcLinePoint(line2,line1,sawWidth,true);
                            if(UtilMisc.isNotNull(focePoint)){
                                path.addTrue(focePoint.getX().doubleValue(), focePoint.getY().doubleValue());
                                pointX.add(focePoint.getX());
                                pointY.add(focePoint.getY());
                            }
                        }else if(line1.getIsArc() && !line2.getIsArc()){
                            //第一个是曲线&第二个是直线
                            //1. 计算起点
                            BigDecimal x1 = pointX.get(pointX.size()-1);
                            BigDecimal y1 = pointY.get(pointY.size()-1);
                            Point focePoint = SVGUtils.getArcLinePoint(line1,line2,sawWidth,false);
                            if(UtilMisc.isNull(focePoint)){
                                continue;
                            }
                            BigDecimal rx = UtilNumber.numberAdd(line1.getRx(), sawWidth);
                            BigDecimal ry = UtilNumber.numberAdd(line1.getRy(), sawWidth);
                            if (line1.getIsArcInwards()) {
                                rx = UtilNumber.numberSubstract(line1.getRx(), sawWidth);
                                ry = UtilNumber.numberSubstract(line1.getRy(), sawWidth);
                            }
                            String pointStrs = SvgUtil.arcToPoints(x1, y1, focePoint.getX(), focePoint.getY(), rx, ry,
                                    line1.getX_axis_rotation(), line1.getLarge_arc_flag(), line1.getSweep_flag());
                            for (String s : pointStrs.split(" ")) {
                                s = s.trim();
                                if (s.indexOf(",") == -1) {
                                    continue;
                                }
                                String[] value = s.split(",");
                                double x = Double.parseDouble(value[0]);
                                double y = Double.parseDouble(value[1]);
                                path.addTrue(x, y);
                            }
                            path.addTrue(focePoint.getX().doubleValue(), focePoint.getY().doubleValue());
                            pointX.add(focePoint.getX());
                            pointY.add(focePoint.getY());
                        }else{
                            //两个都是曲线
                            BigDecimal x1 = pointX.get(pointX.size()-1);
                            BigDecimal y1 = pointY.get(pointY.size()-1);
                            //2. 计算终点
                            Point arcEnd = calculArcEndPoint(line1,sawWidth,polygon);
                            BigDecimal rx = UtilNumber.numberAdd(line1.getRx(), sawWidth);
                            BigDecimal ry = UtilNumber.numberAdd(line1.getRy(), sawWidth);
                            if (line1.getIsArcInwards()) {
                                rx = UtilNumber.numberSubstract(line1.getRx(), sawWidth);
                                ry = UtilNumber.numberSubstract(line1.getRy(), sawWidth);
                            }
                            String pointStrs = SvgUtil.arcToPoints(x1, y1, arcEnd.getX(), arcEnd.getY(), rx, ry,
                                    line1.getX_axis_rotation(), line1.getLarge_arc_flag(), line1.getSweep_flag());
                            for (String s : pointStrs.split(" ")) {
                                s = s.trim();
                                if (s.indexOf(",") == -1) {
                                    continue;
                                }
                                String[] value = s.split(",");
                                double x = Double.parseDouble(value[0]);
                                double y = Double.parseDouble(value[1]);
                                path.addTrue(x, y);
                            }
                            path.addTrue(arcEnd.getX().doubleValue(), arcEnd.getY().doubleValue());
                            pointX.add(arcEnd.getX());
                            pointY.add(arcEnd.getY());
                        }
                    }
                    path.bid = count;
                    path.setRotation(4);
                    path.setIsInline(isInLine);
                    path.setIsIncludeArc(isIncludeArc);
                    nestPaths.add(path);
                }
            }
            param.put("count",count);
            //对每一个SVG文件，作坐标系的平移
            BigDecimal maxX = new BigDecimal(param.get("maxX").toString());
            BigDecimal maxY = new BigDecimal(param.get("maxY").toString());
            BigDecimal lastX = null;
            BigDecimal lastY = null;
            for (NestPath nestPath : nestPaths) {
                Iterator<Segment> segmentIterator = nestPath.getSegments().iterator();
                while (segmentIterator.hasNext()) {
                    Segment segment = segmentIterator.next();
                    double x = segment.getX() + virtualX.doubleValue();
                    double y = segment.getY() + virtualY.doubleValue();
                    if(UtilMisc.isNull(lastX) || UtilMisc.isNull(lastY)){
                        lastX = new BigDecimal(x);
                        lastY = new BigDecimal(y);
                    }else if (lastX.doubleValue() == x && lastY.doubleValue() == y){
                        segmentIterator.remove();
                        continue;
                    }else{
                        lastX = new BigDecimal(x);
                        lastY = new BigDecimal(y);
                    }
                    segment.setX(x);
                    segment.setY(y);
                    if (x > maxX.doubleValue()) {
                        maxX = new BigDecimal(x);
                    }
                    if (y > maxY.doubleValue()) {
                        maxY = new BigDecimal(y);
                    }
                }
            }
            param.put("maxX",maxX);
            param.put("maxY",maxY);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return nestPaths;
    }

    /**
     * 获取弧线向外偏移sawWidth的终点坐标
     *
     * @param arcLine
     * @param sawWidth
     * @return
     */
    public static Point calculArcOffSetEndPoint(Line arcLine,BigDecimal sawWidth,Polygon polygon){
        //由直线的两点式转化为一般式：
        //x(y2-y1)+y(x1-x2) = x1(y2-y1)+y1(x1-x2)  -->  ax + by = c
        //由直线一般式转化为斜截式：
        // ax + by = c  --> y = -(a/b)*x + (c/b)
        BigDecimal a = UtilNumber.numberSubstract(arcLine.getEndPoint().getY(), arcLine.getStartPoint().getY());
        BigDecimal b = UtilNumber.numberSubstract(arcLine.getStartPoint().getX(), arcLine.getEndPoint().getX());
        BigDecimal c = UtilNumber.numberAdd(UtilNumber.numberMultiply(arcLine.getStartPoint().getX(), a),
                UtilNumber.numberMultiply(arcLine.getStartPoint().getY(), b));
        BigDecimal ca = UtilNumber.numberDivideZero(c,a);
        BigDecimal tmpx = UtilNumber.numberSubstract(arcLine.getStartPoint().getX(),ca);
        BigDecimal ab = UtilNumber.numberDivideZero(a,b);

        //与x轴交点(c/a,0)、原终点（start.x,start.y）
        //偏移前终点与x轴交点的距离
        BigDecimal length = new BigDecimal(Math.sqrt(UtilNumber.numberAdd(UtilNumber.numberMultiply(tmpx,tmpx),UtilNumber.numberMultiply(arcLine.getStartPoint().getY(),arcLine.getStartPoint().getY())).doubleValue()));
        BigDecimal tmp = UtilNumber.numberDivideZero(length,UtilNumber.numberAdd(length,sawWidth));
        BigDecimal y = UtilNumber.numberDivideZero(arcLine.getStartPoint().getY(),tmp);

        BigDecimal tmpPointX = UtilNumber.numberDivideZero(new BigDecimal(Math.abs(UtilNumber.numberSubstract(ca,arcLine.getStartPoint().getX()).doubleValue())),tmp);

        BigDecimal x = UtilNumber.numberSubstract(ca,tmpPointX);
        if(UtilNumber.greaterThan(arcLine.getStartPoint().getX(),ca)){
            x = UtilNumber.numberAdd(ca,tmpPointX);
        }
        Point point1 = new Point();
        point1.setX(x);
        point1.setY(y);

        boolean isInPolygon = SVGUtils.isInPolygon(point1,polygon);
        if(!isInPolygon){
            return point1;
        }

        sawWidth = sawWidth.negate();
        tmp = UtilNumber.numberDivideZero(length,UtilNumber.numberAdd(length,sawWidth));
        y = UtilNumber.numberDivideZero(arcLine.getStartPoint().getY(),tmp);

        tmpPointX = UtilNumber.numberDivideZero(new BigDecimal(Math.abs(UtilNumber.numberSubstract(ca,arcLine.getStartPoint().getX()).doubleValue())),tmp);

        x = UtilNumber.numberSubstract(ca,tmpPointX);
        if(UtilNumber.greaterThan(arcLine.getStartPoint().getX(),ca)){
            x = UtilNumber.numberAdd(ca,tmpPointX);
        }
        Point point2 = new Point();
        point2.setX(x);
        point2.setY(y);

        return point2;
    }


    /**
     * 计算弧的终点
     *
     * @param line2
     * @param sawWidth
     * @return
     * @throws Exception
     */
    public static Point calculArcEndPoint(Line line2,BigDecimal sawWidth,Polygon polygon) throws Exception {
        //2.1 获取圆心坐标
        SvgArcObject svgArcObject = SvgArcUtil.svgArcToCenterParam(line2.getStartPoint().getX(),
                line2.getStartPoint().getY(), line2.getRx(), line2.getRy(), line2.getX_axis_rotation(),
                line2.getLarge_arc_flag(), line2.getSweep_flag(), line2.getEndPoint().getX(),
                line2.getEndPoint().getY());
        //2.2 构造终点与圆心所在的直线
        Line arcLine = new Line();
        arcLine.setStartPoint(line2.getEndPoint());
        Point arcPoint = new Point();
        arcPoint.setX(svgArcObject.getCx());
        arcPoint.setY(svgArcObject.getCy());
        arcLine.setEndPoint(arcPoint);
        //2.3 获取终点坐标
        Point arcEnd = calculArcOffSetEndPoint(arcLine,sawWidth,polygon);
        return arcEnd;
    }

    /**
     * 计算当前直线，向外偏移sawWidth后的直线公式
     *
     * @param line
     * @param polygon
     * @param sawWidth
     */
    public static void calculOffSetLine(Line line, Polygon polygon, BigDecimal sawWidth) {
        //由直线的两点式转化为一般式：
        //x(y2-y1)+y(x1-x2) = x1(y2-y1)+y1(x1-x2)  -->  ax + by = c
        //由直线一般式转化为斜截式：
        // ax + by = c  --> y = -(a/b)*x + (c/b)
        BigDecimal a = UtilNumber.numberSubstract(line.getEndPoint().getY(), line.getStartPoint().getY());
        BigDecimal b = UtilNumber.numberSubstract(line.getStartPoint().getX(), line.getEndPoint().getX());
        BigDecimal c = UtilNumber.numberAdd(UtilNumber.numberMultiply(line.getStartPoint().getX(), a),
                UtilNumber.numberMultiply(line.getStartPoint().getY(), b));


        BigDecimal middleX = UtilNumber.numberDivideZero(UtilNumber.numberAdd(line.getStartPoint().getX(),line.getEndPoint().getX()),new BigDecimal(2));
        BigDecimal middleY = UtilNumber.numberDivideZero(UtilNumber.numberAdd(line.getStartPoint().getY(),line.getEndPoint().getY()),new BigDecimal(2));
        if(line.getIsArc()){
            middleX = line.getStartPoint().getX();
            middleY = line.getStartPoint().getY();
        }
        boolean isX = false;
        boolean isY = false;
        if(UtilNumber.equals(line.getStartPoint().getX(),line.getEndPoint().getX())){
            //横坐标相同，则垂直于x轴
            //判断x+1的一点是否在图形内部
            middleX = UtilNumber.numberAdd(middleX,BigDecimal.ONE);
            isX = true;
        }else if(UtilNumber.equals(line.getStartPoint().getY(),line.getEndPoint().getY())){
            //纵坐标相同，则垂直于y轴
            //判断y+1的一点是否在图形内部
            middleY = UtilNumber.numberAdd(middleY,BigDecimal.ONE);
            isY = true;
        }else{
            //先判断中点的纵坐标 +1 后的点，是否在图形内部
            middleY = UtilNumber.numberAdd(middleY,BigDecimal.ONE);
        }
        Point middlePointOffSet = new Point();
        middlePointOffSet.setX(middleX);
        middlePointOffSet.setY(middleY);
        boolean isInPloygon = SVGUtils.isInPolygon(middlePointOffSet,polygon);
        if(isInPloygon){
            if(isX){
                //此时需要向x轴负方向平移 x = c/a - sawWidth
                line.setA(BigDecimal.ONE);
                line.setC(UtilNumber.numberSubstract(UtilNumber.numberDivideZero(c,a),sawWidth));
            }else if(isY){
                //此时需要向y轴负方向平移 y = c/b - sawWidth
                line.setB(BigDecimal.ONE);
                line.setC(UtilNumber.numberSubstract(UtilNumber.numberDivideZero(c,b),sawWidth));
            }else{
                //向着y轴正方向在矩形内部，则需要向y轴负方向平移
                //平移的距离等于 cos angle * sawWidth
                //其中cos angle 是指直线与x轴的夹角
                //斜截式两个点：(0,c/b)、(c/a,0)
                //两点之间的距离：
                BigDecimal cb = UtilNumber.numberDivideZero(c,b);
                BigDecimal ca = UtilNumber.numberDivideZero(c,a);
                BigDecimal length = new BigDecimal(Math.sqrt(UtilNumber.numberAdd(UtilNumber.numberMultiply(cb,cb),UtilNumber.numberMultiply(ca,ca)).doubleValue()));
                BigDecimal tmpOffSet = UtilNumber.numberDivideZero(UtilNumber.numberMultiply(sawWidth,length),new BigDecimal(Math.abs(ca.doubleValue())));
                //此时需要向y轴负方向平移  y = -(a/b)*x + (c/b) - tmpOffSet
                line.setA(UtilNumber.numberDivideZero(a,b));
                line.setB(BigDecimal.ONE);
                line.setC(UtilNumber.numberSubstract(cb,tmpOffSet));
            }
        }else{
            if(isX){
                //此时需要向x轴正方向平移 x = c/a + sawWidth
                line.setA(BigDecimal.ONE);
                line.setC(UtilNumber.numberAdd(UtilNumber.numberDivideZero(c,a),sawWidth));
            }else if(isY){
                //此时需要向y轴正方向平移 y = c/b + sawWidth
                line.setB(BigDecimal.ONE);
                line.setC(UtilNumber.numberAdd(UtilNumber.numberDivideZero(c,b),sawWidth));
            }else{
                //向着y轴负方向在矩形内部，则需要向y轴正方向平移
                //平移的距离等于 cos angle * sawWidth
                //其中cos angle 是指直线与x轴的夹角
                //斜截式两个点：(0,c/b)、(c/a,0)
                //两点之间的距离：
                BigDecimal cb = UtilNumber.numberDivideZero(c,b);
                BigDecimal ca = UtilNumber.numberDivideZero(c,a);
                BigDecimal length = new BigDecimal(Math.sqrt(UtilNumber.numberAdd(UtilNumber.numberMultiply(cb,cb),UtilNumber.numberMultiply(ca,ca)).doubleValue()));
                BigDecimal tmpOffSet = UtilNumber.numberDivideZero(UtilNumber.numberMultiply(sawWidth,length),new BigDecimal(Math.abs(ca.doubleValue())));
                //此时需要向y轴负方向平移  y = -(a/b)*x + (c/b) + tmpOffSet
                line.setA(UtilNumber.numberDivideZero(a,b));
                line.setB(BigDecimal.ONE);
                line.setC(UtilNumber.numberAdd(cb,tmpOffSet));
            }
        }
    }

    /**
     * 计算两条线的交点
     *
     * @param line1
     * @param line2
     * @return
     */
    public static Point getLinePoint(Line line1, Line line2) {
        //由直线的两点式转化为一般式：
        //x(y2-y1)+y(x1-x2) = x1(y2-y1)+y1(x1-x2)  -->  a1x + b1y = c
        BigDecimal a1 = UtilMisc.isNotNull(line1.getA()) ? line1.getA() : BigDecimal.ZERO;
        BigDecimal b1 = UtilMisc.isNotNull(line1.getB()) ? line1.getB() : BigDecimal.ZERO;
        BigDecimal c1 = UtilMisc.isNotNull(line1.getC()) ? line1.getC() : BigDecimal.ZERO;
        if(UtilMisc.isNull(line1.getA()) && UtilMisc.isNull(line1.getB()) && UtilMisc.isNull(line1.getC())){
            a1 = UtilNumber.numberSubstract(line1.getEndPoint().getY(), line1.getStartPoint().getY());
            b1 = UtilNumber.numberSubstract(line1.getStartPoint().getX(), line1.getEndPoint().getX());
            c1 = UtilNumber.numberAdd(UtilNumber.numberMultiply(line1.getStartPoint().getX(), a1),
                    UtilNumber.numberMultiply(line1.getStartPoint().getY(), b1));
        }
        //第二条直线
        BigDecimal a2 = UtilMisc.isNotNull(line2.getA()) ? line2.getA() : BigDecimal.ZERO;
        BigDecimal b2 = UtilMisc.isNotNull(line2.getB()) ? line2.getB() : BigDecimal.ZERO;
        BigDecimal c2 = UtilMisc.isNotNull(line2.getC()) ? line2.getC() : BigDecimal.ZERO;
        if(UtilMisc.isNull(line2.getA()) && UtilMisc.isNull(line2.getB()) && UtilMisc.isNull(line2.getC())){
            a2 = UtilNumber.numberSubstract(line2.getEndPoint().getY(), line2.getStartPoint().getY());
            b2 = UtilNumber.numberSubstract(line2.getStartPoint().getX(), line2.getEndPoint().getX());
            c2 = UtilNumber.numberAdd(UtilNumber.numberMultiply(line2.getStartPoint().getX(), a2),
                    UtilNumber.numberMultiply(line2.getStartPoint().getY(), b2));
        }
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
            Point newPoint = new Point();
            newPoint.setY(y);
            newPoint.setX(x);
            return newPoint;
        } else {
            return null;
        }
    }

}
