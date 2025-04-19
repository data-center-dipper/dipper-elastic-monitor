package com.dipper.monitor.utils.elastic;


 import cn.hutool.core.date.DateField;
 import cn.hutool.core.date.DateTime;
 import cn.hutool.core.util.StrUtil;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.ZoneId;
 import java.time.temporal.ChronoUnit;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.function.BiConsumer;
 import java.util.regex.Pattern;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.tuple.Pair;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;


 public class DateUtil
         {
 private static final Logger log = LoggerFactory.getLogger(DateUtil.class);
    
    
       public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
  private static final ChronoUnit[] PARTITIONS = new ChronoUnit[] { ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS };
    
    
    
  static Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    
       public static Date monthStartDate(Date date) {
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      c.set(5, 0);
      c.set(11, 0);
      c.set(12, 0);
      c.set(13, 0);
      c.set(14, 0);
      return c.getTime();
           }

       public static String getDateBeforeOrAfterDay(int day, boolean zeroBegin) {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         Calendar rightNow = Calendar.getInstance();
         rightNow.add(5, day);

      if (zeroBegin) {
           rightNow.set(11, 0);
           rightNow.set(12, 0);
           rightNow.set(13, 0);
           rightNow.set(14, 0);
                 }
     return sdf.format(rightNow.getTime());
           }
    
       public static Date minInc(Date start, int offset) {
        Calendar c = Calendar.getInstance();
        c.setTime(start);
        c.add(12, offset);
        return c.getTime();
           }
    
       public static Date nextDay(Date start) {
        /*  79 */     Calendar c = Calendar.getInstance();
        /*  80 */     c.setTime(start);
        /*  81 */     c.add(5, 1);
        /*  82 */     return c.getTime();
           }
    
       public static String dateFormat(long timeStamp, String pattern) {
        /*  86 */     SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        /*  87 */     return sdf.format(Long.valueOf(timeStamp));
           }
    
       public static Date toDate(String dateStr, String pattern) {
        /*  91 */     SimpleDateFormat df = new SimpleDateFormat(pattern);
             try {
            /*  93 */       return df.parse(dateStr);
            /*  94 */     } catch (ParseException e) {
            /*  95 */       log.error(e.getMessage(), e);
            
            /*  97 */       return null;
                 }
           }
       public static Date utc2local(String utcTime) {
        /* 101 */     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        /* 102 */     df.setTimeZone(TimeZone.getTimeZone("UTC"));
             try {
            /* 104 */       return df.parse(utcTime);
            /* 105 */     } catch (ParseException e) {
            /* 106 */       log.error(e.getMessage(), e);
            
            /* 108 */       return null;
                 }
           }

       public static String timestamp2localStr(Long timestamp) {
        /* 118 */     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /* 119 */     return df.format(new Date(timestamp.longValue()));
           }
    
       public static String currentDateStr() {
        /* 123 */     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /* 124 */     return df.format(new Date());
           }
    
       public static String utc2localStr(String utcTime) {
        /* 128 */     if (pattern.matcher(utcTime).matches()) {
            /* 129 */       return timestamp2localStr(Long.valueOf(Long.parseLong(utcTime)));
                 }
        /* 131 */     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /* 132 */     Date localDate = utc2local(utcTime);
        /* 133 */     return df.format(localDate);
           }
    
       public static String utc2Str(String utcTime) {
        /* 137 */     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /* 138 */     Date localDate = utc2cst(utcTime);
        /* 139 */     return df.format(localDate);
           }
    
       public static Date utc2cst(String utcTime) {
        /* 143 */     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /* 144 */     df.setTimeZone(TimeZone.getTimeZone("UTC"));
             try {
            /* 146 */       return df.parse(utcTime);
            /* 147 */     } catch (ParseException e) {
            /* 148 */       log.error(e.getMessage(), e);
            
            /* 150 */       return null;
                 }
           }
       public static Long str2Long(String time) {
        /* 154 */     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /* 155 */     Date localDate = null;
             try {
            /* 157 */       localDate = df.parse(time);
            /* 158 */     } catch (ParseException e) {
            /* 159 */       log.error(e.getMessage(), e);
                 }
        /* 161 */     if (null != localDate)
            /* 162 */       return Long.valueOf(localDate.getTime());
        /* 163 */     return Long.valueOf(0L);
           }
    
       public static String getDateTime(Date date) {
        /* 167 */     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /* 168 */     return df.format(date);
           }

       public static Pair<Long, ChronoUnit> dateInterval(Date startTime, Date endTime, long part) {
        /* 179 */     ZoneId zone = ZoneId.systemDefault();
        /* 180 */     LocalDateTime start = LocalDateTime.ofInstant(startTime.toInstant(), zone);
        /* 181 */     LocalDateTime end = LocalDateTime.ofInstant(endTime.toInstant(), zone);
        /* 182 */     for (ChronoUnit t : PARTITIONS) {
            /* 183 */       long d = t.between(start, end);
            /* 184 */       if (d >= part) {
                /* 185 */         long interval = d / part;
                /* 186 */         long last = d % part;
                
                /* 188 */         if (last <= interval)
                             {
                    
                    /* 191 */           return Pair.of(Long.valueOf(interval), t); }
                       }
                 }
        /* 194 */     return Pair.of(Long.valueOf(1L), ChronoUnit.MINUTES);
           }

       public static Date truncatedTo(Date date, ChronoUnit unit) {
        /* 206 */     ZoneId zone = ZoneId.systemDefault();
        /* 207 */     LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), zone);
        /* 208 */     if (unit == ChronoUnit.YEARS) {
            /* 209 */       return Date.from(LocalDate.of(dateTime.getYear(), 1, 1).atStartOfDay(zone).toInstant());
                 }
        /* 211 */     if (unit == ChronoUnit.MONTHS) {
            /* 212 */       return Date.from(LocalDate.of(dateTime.getYear(), dateTime.getMonth(), 1).atStartOfDay(zone).toInstant());
                 }
        /* 214 */     return Date.from(dateTime.truncatedTo(unit).atZone(zone).toInstant());
           }

       public static void createTimes(Date begin, Date end, long interval, ChronoUnit unit, BiConsumer<Integer, LocalDateTime> func) {
        /* 232 */     ZoneId zone = ZoneId.systemDefault();
        /* 233 */     LocalDateTime timeBegin = LocalDateTime.ofInstant(begin.toInstant(), zone);
        /* 234 */     LocalDateTime timeEnd = LocalDateTime.ofInstant(end.toInstant(), zone);
        /* 235 */     int idx = 0;
        /* 236 */     while (timeBegin.compareTo(timeEnd) <= 0) {
            /* 237 */       func.accept(Integer.valueOf(idx), timeBegin);
            /* 238 */       timeBegin = timeBegin.plus(interval, unit);
            /* 239 */       idx++;
                 }
           }
    
       public static String parseTime(Object pto) {
             String processTime;
        /* 245 */     if (pto instanceof Long) {
            /* 246 */       processTime = dateFormat(((Long)pto).longValue(), "yyyy-MM-dd HH:mm:ss");
                 } else {
            /* 248 */       processTime = utc2localStr((String)pto);
                 }
        /* 250 */     return processTime;
           }
    
       public static DateTime strDateOffset(String date, String pattern, DateField dateField, int offset) {
        /* 254 */     if (StrUtil.isBlank(date)) {
            /* 255 */       return null;
                 }
        /* 257 */     return (new DateTime(toDate(date, pattern))).offset(dateField, offset);
           }
       public static DateTime defaultStrDateOffset(String date, DateField dateField, int offset) {
        /* 260 */     if (StrUtil.isBlank(date)) {
            /* 261 */       return null;
                 }
        /* 263 */     return strDateOffset(date, "yyyy-MM-dd HH:mm:ss", dateField, offset);
           }
    
       public static DateTime defaultStrDateOffsetHour(String date, int offset) {
        /* 267 */     if (StrUtil.isBlank(date)) {
            /* 268 */       return null;
                 }
        /* 270 */     return defaultStrDateOffset(date, DateField.HOUR_OF_DAY, offset);
           }
    
       public static long getNearly5M(long time) {
        /* 274 */     long m5 = 300000L;
        /* 275 */     return time / m5 * m5;
           }
    
       public static Timestamp strToTimestamp(String createtime) {
        /* 279 */     if (StringUtils.isBlank(createtime)) return null;
        /* 280 */     DateTime parse = cn.hutool.core.date.DateUtil.parse(createtime, "yyyy-MM-dd HH:mm:ss");
        /* 281 */     return parse.toTimestamp();
           }
     }

