package com.aries.util.nest.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class UtilMisc {


    /**
     * <pre>
     * 判断2个类型是否相同, 如果2个参数均为null，认为不想相等
     *
     * 如: equalsRegardingNull(null, null)=false
     *
     * @param expected
     * @param actual
     * @return
     */
    public static boolean equals(Object expected, Object actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return isEquals(expected, actual);
    }

    private static boolean isEquals(Object expected, Object actual) {
        return expected.equals(actual);
    }



    /**
     * 判断Map是否为null或 size = 0
     *
     * @param map
     * @return
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判判断Map不为null并且 size != 0
     *
     * @param map
     * @return
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 判断字符串是否为null 或空串，如果是空格空格也认为是空
     *
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0 || s.trim().length() == 0;
    }

    /**
     * 判断字符串非空
     *
     * @param s
     * @return
     */
    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    /**
     * 判断对象是否为空
     *
     * @param obj
     * @return
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * 判断对象是否不为空
     *
     * @param obj
     * @return
     */
    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    /**
     * 判断集合是否为空
     *
     * @param c
     * @return
     */
    public static boolean isEmpty(Collection<?> c) {
        if (c == null || c.isEmpty())
            return true;
        return false;
    }

    /**
     * 判断集合是否为空
     *
     * @param c
     * @return
     */
    public static boolean isNotEmpty(Collection<?> c) {
        return !isEmpty(c);
    }

    /**
     * 判断数组是否为空
     *
     * @param arr
     * @return
     */
    public static boolean isEmpty(Object[] arr) {
        if (arr == null || arr.length == 0)
            return true;
        return false;
    }

    /**
     * 判断数组是否为空
     *
     * @param arr
     * @return
     */
    public static boolean isNotEmpty(Object[] arr) {
        return !isEmpty(arr);
    }


    /**
     * 判断一个数值是否为null或者0
     *
     * @param num
     * @return
     */
    public static boolean isNullOrZero(Number num) {
        if (num == null || num.equals(0L))
            return true;
        if (num instanceof Integer) {
            return ((Integer) num).compareTo(0) == 0;
        }
        if (num instanceof Long) {
            return ((Long) num).compareTo(0L) == 0;
        }
        if (num instanceof BigDecimal) {
            return (((BigDecimal) num).compareTo(BigDecimal.ZERO) == 0);
        }
        return false;
    }


    public static int doWaitFor(Process process) {
        InputStream in = null;
        InputStream err = null;
        int exitValue = -1; // returned to caller when p is finished
        try {
            in = process.getInputStream();
            err = process.getErrorStream();
            boolean finished = false; // Set to true when p is finished
            while (!finished) {
                try {
                    while (in.available() > 0) {
                        // Print the output of our system call
                        Character c = new Character((char) in.read());
                        System.out.print(c);
                    }
                    while (err.available() > 0) {
                        // Print the output of our system call
                        Character c = new Character((char) err.read());
                        System.out.print(c);
                    }
                    // Ask the process for its exitValue. If the process
                    // is not finished, an IllegalThreadStateException
                    // is thrown. If it is finished, we fall through and
                    // the variable finished is set to true.
                    exitValue = process.exitValue();
                    finished = true;
                } catch (IllegalThreadStateException e) {
                    // Process is not finished yet;
                    // Sleep a little to save on CPU cycles
                    Thread.currentThread().sleep(500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (err != null) {
                try {
                    err.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return exitValue;
    }
}
