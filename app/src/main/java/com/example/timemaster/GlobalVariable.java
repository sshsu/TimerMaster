package com.example.timemaster;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GlobalVariable {
    public static int timeDuraTion = 1000;  //时间增加减少的单位 1000ms
    public static DBOperatorHelper DBhelpr;

    public static String getTimeYYMMDDHHMMSS(){
        String ret_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).toString();
        return ret_time;
    }

    public static int getTimeSec(){
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int min = now.get(Calendar.MINUTE);
        int sec = now.get(Calendar.SECOND);
        int ret_time = hour * 60 * 60 + min * 60 + sec;
        return ret_time;
    }

    public static String getTimeYYMMDD(){
        String ret_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
        return ret_time;
    }
}
