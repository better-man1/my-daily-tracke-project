package com.dailytracker.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * 日期工具类
 */
public class DateUtils {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");

    private DateUtils() {}

    /** 获取今天日期 */
    public static LocalDate today() {
        return LocalDate.now(ZONE_SHANGHAI);
    }

    /** 获取当前时间 */
    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE_SHANGHAI);
    }

    /** 获取本周开始日期（周一） */
    public static LocalDate weekStart() {
        return today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** 获取本周结束日期（周日） */
    public static LocalDate weekEnd() {
        return today().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /** 获取本月开始日期 */
    public static LocalDate monthStart() {
        return today().with(TemporalAdjusters.firstDayOfMonth());
    }

    /** 获取本月结束日期 */
    public static LocalDate monthEnd() {
        return today().with(TemporalAdjusters.lastDayOfMonth());
    }

    /** 获取本年开始日期 */
    public static LocalDate yearStart() {
        return today().with(TemporalAdjusters.firstDayOfYear());
    }

    /** 获取本年结束日期 */
    public static LocalDate yearEnd() {
        return today().with(TemporalAdjusters.lastDayOfYear());
    }

    /** 格式化日期 */
    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    /** 格式化日期时间 */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    /** 解析日期字符串 */
    public static LocalDate parseDate(String dateStr) {
        return dateStr == null ? null : LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /** 获取指定日期所在月份的开始 */
    public static LocalDate monthStart(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    /** 获取指定日期所在月份的结束 */
    public static LocalDate monthEnd(int year, int month) {
        return LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());
    }

    /** 计算两个日期之间的天数 */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return end.toEpochDay() - start.toEpochDay();
    }
}
