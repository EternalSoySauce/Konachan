package com.ess.anime.wallpaper.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static Date getToday(int realYear, int realMonth, int realDay) {
        return getCalendarByData(realYear, realMonth, realDay).getTime();
    }

    public static Date getNextDay(int realYear, int realMonth, int realDay) {
        Calendar calendar = getCalendarByData(realYear, realMonth, realDay);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    public static Date getNextDay(Date date) {
        Calendar calendar = getCalendarByDate(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    public static Date getMondayOfWeek(int realYear, int realMonth, int realDay) {
        Calendar calendar = getCalendarByData(realYear, realMonth, realDay);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return calendar.getTime();
    }

    // Calendar周日是每周的第一天，此处需要周日为每周的最后一天
    public static Date getSundayOfWeek(int realYear, int realMonth, int realDay) {
        Calendar calendar = getCalendarByData(realYear, realMonth, realDay);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return calendar.getTime();
    }

    public static Date getFirstDayOfMonth(int realYear, int realMonth, int realDay) {
        Calendar calendar = getCalendarByData(realYear, realMonth, realDay);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static Date getLastDayOfMonth(int realYear, int realMonth, int realDay) {
        Calendar calendar = getCalendarByData(realYear, realMonth, realDay);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    public static Calendar getCalendarByData(int realYear, int realMonth, int realDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(realYear, realMonth - 1, realDay);  // calendar月份从0开始，所以需要减1
        calendar.setFirstDayOfWeek(Calendar.MONDAY);  // 以周一为每周第一天
        return calendar;
    }

    public static Calendar getCalendarByDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);  // 以周一为每周第一天
        return calendar;
    }
}
