package com.aries.util.nest.util;

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
}
