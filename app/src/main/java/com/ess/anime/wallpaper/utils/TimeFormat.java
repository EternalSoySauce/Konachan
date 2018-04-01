package com.ess.anime.wallpaper.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeFormat {

    // 格式化为 00:00:00
    public static String durationFormat(long msec) {
        msec = (long) (msec / 1000f + 0.5f);
        long hour = msec / 3600;
        long minute = msec % 3600 / 60;
        long second = msec % 3600 % 60;

        return hour == 0
                ? String.format(Locale.getDefault(), "%02d:%02d", minute, second)
                : String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
    }

    // 格式化为 00:00:00.0
    public static String durationFormat2(long msec) {
        long millisecond = (long) (msec % 1000 / 100f + 0.5f);
        msec /= 1000;
        long hour = msec / 3600;
        long minute = msec % 3600 / 60;
        long second = msec % 3600 % 60;

        return hour == 0
                ? String.format(Locale.getDefault(), "%02d:%02d.%1d", minute, second, millisecond)
                : String.format(Locale.getDefault(), "%02d:%02d:%02d.%1d", hour, minute, second, millisecond);
    }

    // 格式化日期为 format 形式
    public static String dateFormat(long msec, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date(msec);
        return dateFormat.format(date);
    }

    // string格式转换为long (msec)
    public static long stringToLong(String time, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        try {
            Date date = dateFormat.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
