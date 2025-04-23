package com.dipper.monitor.utils.elastic;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EsDateUtils {
    public static int getNowDateInt(String pattern) {
        Date now = new Date();
        SimpleDateFormat sfd = new SimpleDateFormat(pattern);
        String nowDate = sfd.format(now);
        int nowDateInt = Integer.parseInt(nowDate);
        return nowDateInt;
    }
}
