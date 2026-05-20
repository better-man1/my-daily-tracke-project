package com.dailytracker.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * 日期时间工具类 - 提供常用的日期时间操作方法
 *
 * 【类的用途】
 * 封装项目中频繁使用的日期时间操作，包括：
 * 1. 获取特定时区的当前日期和时间
 * 2. 获取本周/本月/本年的起止日期
 * 3. 日期的格式化和解析
 * 4. 计算两个日期之间的天数
 *
 * 【设计思想】
 * 1. 统一时区：所有日期时间操作都基于上海时区（Asia/Shanghai），
 *    避免因服务器时区不同导致数据不一致
 * 2. 使用 Java 8+ 的 java.time API（替代旧的 Date/Calendar）：
 *    - 不可变对象（线程安全）
 * - 更清晰的 API 设计
 *    - 更精确的时间处理
 * 3. 工具类模式：私有构造函数 + 静态方法 + 常量
 *
 * 【在架构中的位置】
 * 属于工具层，被 Service 层广泛使用。
 *
 * 【相关技术知识点 - Java 8 日期时间 API】
 * 1. LocalDate：日期（年-月-日），不包含时间和时区信息
 * 2. LocalDateTime：日期时间（年-月-日 时:分:秒），不包含时区信息
 * 3. ZoneId：时区标识，如 Asia/Shanghai、America/New_York
 * 4. DateTimeFormatter：日期时间格式化器（替代 SimpleDateFormat）
 * 5. TemporalAdjusters：日期调整器，用于计算"本月第一天"、"下个周一"等
 *
 * 【为什么不用 Date 和 Calendar？】
 * - Date 是可变的（非线程安全），月份从 0 开始（反直觉）
 * - Calendar 也是可变的，API 设计冗余复杂
 * - java.time 是 Java 8 引入的新 API，解决了上述所有问题
 */
public class DateUtils {

    /**
     * 日期格式化器：yyyy-MM-dd（如 2024-01-15）
     * DateTimeFormatter 是线程安全的（与 SimpleDateFormat 不同），可以定义为静态常量
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 日期时间格式化器：yyyy-MM-dd HH:mm:ss（如 2024-01-15 10:30:00）
     * 用于将 LocalDateTime 格式化为字符串，或从字符串解析为 LocalDateTime
     */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 上海时区 - 中国标准时间（UTC+8）
     * 所有日期时间操作都基于此时区，确保不受服务器运行环境影响
     */
    public static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");

    /**
     * 私有构造函数 - 禁止实例化工具类
     */
    private DateUtils() {}

    /**
     * 获取当前日期（上海时区）
     *
     * @return LocalDate 当前日期，如 2024-01-15
     */
    public static LocalDate today() {
        return LocalDate.now(ZONE_SHANGHAI);   // 指定时区获取当前日期
    }

    /**
     * 获取当前日期时间（上海时区）
     *
     * @return LocalDateTime 当前日期时间，如 2024-01-15T10:30:00
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE_SHANGHAI);
    }

    /**
     * 获取本周的开始日期（周一）
     *
     * 【方法作用】
     * 获取本周一的日期。中国习惯以周一为一周的第一天。
     *
     * TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)：
     * 如果今天是周一，返回今天；否则返回上周一
     * 效果：总是返回本周一的日期
     *
     * @return LocalDate 本周一的日期
     */
    public static LocalDate weekStart() {
        return today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * 获取本周的结束日期（周日）
     *
     * @return LocalDate 本周日的日期
     */
    public static LocalDate weekEnd() {
        return today().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * 获取本月的开始日期（本月1号）
     *
     * @return LocalDate 本月第一天的日期
     */
    public static LocalDate monthStart() {
        return today().with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取本月的结束日期（本月最后一天）
     *
     * @return LocalDate 本月最后一天的日期
     */
    public static LocalDate monthEnd() {
        return today().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取本年的开始日期（1月1日）
     *
     * @return LocalDate 本年1月1日的日期
     */
    public static LocalDate yearStart() {
        return today().with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * 获取本年的结束日期（12月31日）
     *
     * @return LocalDate 本年12月31日的日期
     */
    public static LocalDate yearEnd() {
        return today().with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * 将日期格式化为字符串（yyyy-MM-dd）
     *
     * @param date 要格式化的日期，可以为 null
     * @return String 格式化后的字符串（如 "2024-01-15"），如果 date 为 null 则返回 null
     */
    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    /**
     * 将日期时间格式化为字符串（yyyy-MM-dd HH:mm:ss）
     *
     * @param dateTime 要格式化的日期时间，可以为 null
     * @return String 格式化后的字符串（如 "2024-01-15 10:30:00"），如果 dateTime 为 null 则返回 null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 将字符串解析为日期（yyyy-MM-dd）
     *
     * @param dateStr 日期字符串，如 "2024-01-15"，可以为 null
     * @return LocalDate 解析后的日期，如果 dateStr 为 null 则返回 null
     * @throws java.time.format.DateTimeParseException 如果字符串格式不正确
     */
    public static LocalDate parseDate(String dateStr) {
        return dateStr == null ? null : LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 获取指定年份和月份的第一天
     *
     * @param year  年份，如 2024
     * @param month 月份（1-12），如 1 表示一月
     * @return LocalDate 该月第一天的日期
     */
    public static LocalDate monthStart(int year, int month) {
        return LocalDate.of(year, month, 1);   // 每月第一天就是 1 号
    }

    /**
     * 获取指定年份和月份的最后一天
     *
     * 【注意】自动处理不同月份的天数：
     * - 1月31天、2月28/29天、3月31天等
     * - 闰年的2月有29天
     *
     * @param year  年份
     * @param month 月份（1-12）
     * @return LocalDate 该月最后一天的日期
     */
    public static LocalDate monthEnd(int year, int month) {
        // 先创建该月1号的日期，然后用 TemporalAdjusters 获取最后一天
        return LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 计算两个日期之间的天数差
     *
     * 【方法作用】
     * 计算 start 到 end 之间的天数（end - start）。
     * 结果可以为负数（如果 end 在 start 之前）。
     *
     * 【实现原理】
     * toEpochDay() 将日期转换为从 1970-01-01 开始的天数（纪元日），
     * 两个纪元日相减即为天数差。这种方法简洁且无溢出风险。
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return long 天数差（正数表示 end 在 start 之后，负数表示之前）
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return end.toEpochDay() - start.toEpochDay();
    }
}
