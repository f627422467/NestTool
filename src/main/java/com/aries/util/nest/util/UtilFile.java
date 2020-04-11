package com.aries.util.nest.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class UtilFile {



    public static String readFile(File file) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        FileInputStream fileInputStream = new FileInputStream(file);
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
        line = reader.readLine();
        while (line != null) {
            stringBuffer.append(line);
            line = reader.readLine();
        }
        reader.close();
        fileInputStream.close();
        return stringBuffer.toString();
    }

}
