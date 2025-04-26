package com.dipper.monitor.utils.elastic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.time.FastDateFormat;

public class EsDateUtils {

 
    
    public static int getNowDateInt(String pattern) {
        Date now = new Date();
        SimpleDateFormat sfd = new SimpleDateFormat(pattern);
        String nowDate = sfd.format(now);
        int nowDateInt = Integer.parseInt(nowDate);
        return nowDateInt;
    }

    public static long getNowDateSub(int nowDate, int oldDate, String pattern) throws ParseException {
        SimpleDateFormat sfd = new SimpleDateFormat(pattern);
        Date nowDateTime = sfd.parse("" + nowDate);
        Date oldDateTime = sfd.parse("" + oldDate);
        long diff = nowDateTime.getTime() - oldDateTime.getTime();
        long days = diff / 86400000L;
        return days;
    }

    public static Date calculateExpireTimeByPattern(String pattern, int value) {
        Calendar expireTime = Calendar.getInstance();
        expireTime.set(11, 0);
        expireTime.set(12, 0);
        expireTime.set(13, 0);
        expireTime.set(14, 0);

        switch (pattern) {
            case "yyyyMMdd":
                expireTime.add(6, -1 * value);
                return expireTime.getTime();
            case "yyyyMM":
                expireTime.add(2, -1 * value);
                expireTime.set(5, 1);
                return expireTime.getTime();
            case "yyyy":
                expireTime.add(1, -1 * value);
                expireTime.set(5, 1);
                expireTime.set(2, 0);
                return expireTime.getTime();
        }
        throw new IllegalArgumentException("非法日期格式：" + pattern);
    }

    public static Date analyzeAndCompleteIndexDate(String indexName) throws ParseException {
        String date = getDateOfIndex(indexName);
        Date parseDate = null;
        Calendar indexDate = Calendar.getInstance();
        switch (date.length()) {
            case 8:
                parseDate = FastDateFormat.getInstance("yyyyMMdd").parse(date);
                indexDate.setTime(parseDate);
                indexDate.add(6, 1);
                break;
            case 6:
                parseDate = FastDateFormat.getInstance("yyyyMM").parse(date);
                indexDate.setTime(parseDate);
                indexDate.add(2, 1);
                indexDate.set(5, 1);
                break;
            case 4:
                parseDate = FastDateFormat.getInstance("yyyy").parse(date);
                indexDate.setTime(parseDate);
                indexDate.add(1, 1);
                indexDate.set(5, 1);
                indexDate.set(2, 0);
                break;
        }
        if (parseDate == null) {
            throw new IllegalArgumentException("非法的索引名：" + indexName);
        }
        indexDate.set(11, 0);
        indexDate.set(12, 0);
        indexDate.set(13, 0);
        indexDate.set(14, 0);
        indexDate.add(14, -1);
        return indexDate.getTime();
    }

    public static String getDateOfIndex(String index) {
        String[] parts = index.split("-");
        return parts[parts.length - 2];
    }

    public static String getFormattedDate(String format) {
        return null;
    }
}