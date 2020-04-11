package com.aries.util.nest.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 数值处理，部分数据需要在double和int/long中转换
 */
public class UtilNumber {


    /**
     * 2个数值相加
     *
     * @param a
     * @param b
     * @return
     */
    public static BigDecimal numberAdd(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return BigDecimal.ZERO;
        } else if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        return a.add(b);

    }

    /**
     * 多个数值相加
     *
     * @param nums
     * @return
     */
    public static BigDecimal numberAdd(BigDecimal... nums) {
        BigDecimal sum = BigDecimal.ZERO;
        if (UtilMisc.isNotEmpty(nums)) {
            for (BigDecimal num : nums) {
                sum = numberAdd(sum, num);
            }
        }
        return sum;
    }

    /**
     * 2个数值相加不能是负数
     *
     * @param a
     * @param b
     * @return
     */
    public static BigDecimal numberAddZero(BigDecimal a, BigDecimal b) {
        BigDecimal add = BigDecimal.ZERO;
        if (a == null && b == null) {
            add = BigDecimal.ZERO;
        } else if (a == null) {
            add = b;
        } else if (b == null) {
            add = a;
        } else {
            add = a.add(b);
        }
        if (UtilMisc.isNullOrZero(add) || add.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        } else {
            return add;
        }

    }

    /**
     * 两个数值相减，如果数值为null,设置默认为0
     *
     * @param a
     * @param b
     * @return
     */
    public static BigDecimal numberSubstract(BigDecimal a, BigDecimal b) {
        if (a == null)
            a = BigDecimal.ZERO;
        if (b == null)
            b = BigDecimal.ZERO;
        return a.subtract(b);
    }

    /**
     * 连减
     *
     * @param nums
     * @return
     */
    public static BigDecimal numberSubstract(BigDecimal... nums) {
        BigDecimal result = BigDecimal.ZERO;
        if (UtilMisc.isNotEmpty(nums)) {
            result = nums[0];
            if (nums.length > 1) {
                for (int i = 1; i < nums.length; i++) {
                    result = numberSubstract(result, nums[i]);
                }
            }
        }
        return result;
    }


    /**
     * 2个数值相乘
     *
     * @param a
     * @param b
     * @return
     */
    public static BigDecimal numberMultiply(BigDecimal a, BigDecimal b) {
        if (a == null)
            return BigDecimal.ZERO;
        if (b == null)
            return BigDecimal.ZERO;
        return a.multiply(b);
    }

    /**
     * 多个数相乘,有任意乘数为0或空,返回0
     *
     * @param nums
     * @return
     */
    public static BigDecimal numberMultiply(BigDecimal... nums) {
        BigDecimal sum = BigDecimal.ONE;
        if (UtilMisc.isEmpty(nums)) {
            return BigDecimal.ZERO;
        }
        for (BigDecimal num : nums) {
            sum = numberMultiply(sum, num);
        }
        return sum;
    }


    /**
     * a/b 最大小数位数设置为15
     *
     * @param a
     * @param b
     * @return
     */
    public static BigDecimal numberDivideZero(BigDecimal a, BigDecimal b) {
        if (a == null)
            return BigDecimal.ZERO;
        if (UtilMisc.isNullOrZero(b))
            return BigDecimal.ZERO;
        return numberDivide(a, b, 15);
    }

    /**
     * a/b 最大小数位数设置为15
     *
     * @param a
     * @param b
     * @return
     */
    public static BigDecimal numberDivide(BigDecimal a, BigDecimal b) {
        return numberDivide(a, b, 15);
    }

    public static BigDecimal numberDivide(BigDecimal a, BigDecimal b, int scale) {
        return numberDivide(a, b, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal numberDivide(BigDecimal a, BigDecimal b, int scale, RoundingMode mode) {
        if (a == null)
            return null;
        if (a.equals(BigDecimal.ZERO))
            return BigDecimal.ZERO;
        if (UtilMisc.isNullOrZero(b))
            return null;
        return a.divide(b, scale, mode);
    }


    /**
     * 大于
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean greaterThan(BigDecimal a, BigDecimal b) {
        if (numberSubstract(a, b).compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        return false;
    }

    /**
     * a小于b
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean lessThan(BigDecimal a, BigDecimal b) {
        if (numberSubstract(a, b).compareTo(BigDecimal.ZERO) < 0) {
            return true;
        }
        return false;
    }

    /**
     * a小于等于b
     */
    public static boolean lessEqualThan(BigDecimal a, BigDecimal b) {
        if (numberSubstract(a, b).compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return false;
    }

    /**
     * a大于等于b
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean greaterEqualThan(BigDecimal a, BigDecimal b) {
        if (numberSubstract(a, b).compareTo(BigDecimal.ZERO) >= 0) {
            return true;
        }
        return false;
    }


    public static boolean equals(BigDecimal a, BigDecimal b) {
        if (numberSubstract(a, b).compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        return false;
    }



    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) >= 0)
            return a;
        return b;
    }

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) <= 0)
            return a;
        return b;
    }


    public static boolean isNullOrZero(BigDecimal b) {
        return b == null || isZero(b);
    }

    public static boolean isZero(BigDecimal b) {
        return (b != null && b.compareTo(BigDecimal.ZERO) == 0);
    }
}
