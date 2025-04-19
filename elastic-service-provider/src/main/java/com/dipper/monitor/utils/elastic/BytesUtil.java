package com.dipper.monitor.utils.elastic;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BytesUtil 提供了将字节数转换为带单位的字符串表示形式的方法。
 */
public class BytesUtil {

    /**
     * 将千字节(KB)转换为带有适当单位的字符串表示形式。
     *
     * @param kb 要转换的千字节数
     * @return 带有单位的字符串表示形式
     */
    public static String transKbWithUnit(float kb) {
        if (kb == 0.0F) {
            return "0";
        }
        if (kb < 1024.0F) {
            return formatFloat(kb) + "K";
        } else {
            return transMbWithUnit(kb / 1024.0F);
        }
    }

    /**
     * 将兆字节(MB)转换为带有适当单位的字符串表示形式。
     *
     * @param mb 要转换的兆字节数
     * @return 带有单位的字符串表示形式
     */
    public static String transMbWithUnit(float mb) {
        if (mb < 1024.0F) {
            return formatFloat(mb) + "M";
        } else if (mb < 1048576.0F) { // 1024^2 = 1048576
            return formatFloat(mb / 1024.0F) + "G";
        } else {
            return formatFloat(mb / 1024.0F / 1024.0F) + "T";
        }
    }

    /**
     * 将兆字节(MB)转换为吉字节(GB)，并返回格式化的浮点数。
     *
     * @param mb 要转换的兆字节数
     * @return 格式化后的浮点数
     */
    public static Float transMbToGb(float mb) {
        return formatFloat(mb / 1024.0F);
    }

    /**
     * 格式化浮点数，保留一位小数，并采用向下取整的方式舍入。
     *
     * @param value 要格式化的浮点数值
     * @return 格式化后的浮点数值
     */
    public static float formatFloat(float value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return bd.setScale(1, RoundingMode.FLOOR).floatValue();
    }

    public static Float transMb(float mb) {
       return Float.valueOf(formatFloat(mb / 1024.0F));
     }
}