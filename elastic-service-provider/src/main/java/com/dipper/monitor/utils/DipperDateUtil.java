package com.dipper.monitor.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.CallableStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class DipperDateUtil {
    private static ThreadLocal<DateFormat> df0 = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    private static ThreadLocal<DateFormat> dfMill = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public static String now() {
        return df0.get().format(new Date());
    }

    public static String format(long timestamp) {
        return dfMill.get().format(new Date(timestamp));
    }

    public static boolean isDateFormat0(String timeStr) {
        return false;
    }


    public static String replaceT(String startTime) {
        if(StringUtils.isBlank(startTime)){
            return startTime;
        }
        if(startTime.contains("T")){
            startTime =   startTime.replace("T", " ");
        }
        if(startTime.contains("/")){
            startTime =   startTime.replace("/", "-");
        }
        return startTime;
    }

    public static long toTimestamp(String dateTimeStr) {
        try {
            // 使用 DateTimeFormatter 解析字符串
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            // 如果需要考虑时区，可以根据需要选择合适的 ZoneId
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
            // 转换为 Instant 对象
            Instant instant = zonedDateTime.toInstant();
            // 获取毫秒级别的时间戳
            long unixTimestampInMilliSeconds = instant.toEpochMilli();
            return unixTimestampInMilliSeconds;
        }catch (Exception e){
            log.info("数据转换异常 dateTimeStr:{}",dateTimeStr,e);
            return 0;
        }
    }
}