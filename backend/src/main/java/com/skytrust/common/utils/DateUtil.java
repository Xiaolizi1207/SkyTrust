package com.skytrust.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 *
 * @author SkyTrust Team
 */
public class DateUtil {

    // 常用日期时间格式
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_FORMAT_COMPACT = "yyyyMMddHHmmss";
    public static final String DATETIME_FORMAT_WITH_MS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_FORMAT_CN = "yyyy年MM月dd日";
    public static final String DATETIME_FORMAT_CN = "yyyy年MM月dd日 HH时mm分ss秒";

    // 常用格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER_COMPACT = DateTimeFormatter.ofPattern(DATETIME_FORMAT_COMPACT);

    private DateUtil() {
        // 工具类，防止实例化
    }

    /**
     * 获取当前日期时间
     *
     * @return LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期
     *
     * @return LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 格式化日期时间为字符串（yyyy-MM-dd HH:mm:ss）
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期时间为字符串（指定格式）
     *
     * @param dateTime 日期时间
     * @param pattern  格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化日期为字符串（yyyy-MM-dd）
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期为字符串（指定格式）
     *
     * @param date    日期
     * @param pattern 格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null || pattern == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析字符串为日期时间（yyyy-MM-dd HH:mm:ss）
     *
     * @param str 日期时间字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String str) {
        if (StringUtil.isEmpty(str)) {
            return null;
        }
        try {
            return LocalDateTime.parse(str, DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析字符串为日期时间（指定格式）
     *
     * @param str     日期时间字符串
     * @param pattern 格式
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String str, String pattern) {
        if (StringUtil.isEmpty(str) || pattern == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析字符串为日期（yyyy-MM-dd）
     *
     * @param str 日期字符串
     * @return LocalDate
     */
    public static LocalDate parseDate(String str) {
        if (StringUtil.isEmpty(str)) {
            return null;
        }
        try {
            return LocalDate.parse(str, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析字符串为日期（指定格式）
     *
     * @param str     日期字符串
     * @param pattern 格式
     * @return LocalDate
     */
    public static LocalDate parseDate(String str, String pattern) {
        if (StringUtil.isEmpty(str) || pattern == null) {
            return null;
        }
        try {
            return LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Date 转 LocalDateTime
     *
     * @param date Date对象
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Date 转 LocalDate
     *
     * @param date Date对象
     * @return LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param dateTime LocalDateTime
     * @return Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate 转 Date
     *
     * @param date LocalDate
     * @return Date
     */
    public static Date toDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 计算两个日期时间之间的天数差
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 天数差
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的小时差
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的分钟差
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 增加天数
     *
     * @param dateTime 原始时间
     * @param days     增加的天数（可为负数）
     * @return 增加后的时间
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }

    /**
     * 增加小时
     *
     * @param dateTime 原始时间
     * @param hours    增加的小时数（可为负数）
     * @return 增加后的时间
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    /**
     * 增加分钟
     *
     * @param dateTime 原始时间
     * @param minutes  增加的分钟数（可为负数）
     * @return 增加后的时间
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }

    /**
     * 获取指定日期的开始时间（00:00:00）
     *
     * @param date 日期
     * @return 开始时间
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * 获取指定日期的结束时间（23:59:59.999999999）
     *
     * @param date 日期
     * @return 结束时间
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59, 999999999);
    }

    /**
     * 获取当天的开始时间
     *
     * @return 当天的开始时间
     */
    public static LocalDateTime getStartOfToday() {
        return LocalDate.now().atStartOfDay();
    }

    /**
     * 获取当天的结束时间
     *
     * @return 当天的结束时间
     */
    public static LocalDateTime getEndOfToday() {
        return LocalDate.now().atTime(23, 59, 59, 999999999);
    }

    /**
     * 判断是否为同一天
     *
     * @param dateTime1 时间1
     * @param dateTime2 时间2
     * @return 是否为同一天
     */
    public static boolean isSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return false;
        }
        return dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
    }

    /**
     * 判断日期是否在今天之前
     *
     * @param date 日期
     * @return 是否在今天之前
     */
    public static boolean isBeforeToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }

    /**
     * 判断日期是否在今天之后
     *
     * @param date 日期
     * @return 是否在今天之后
     */
    public static boolean isAfterToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }

    /**
     * 判断日期时间是否在指定时间范围内
     *
     * @param dateTime 要判断的时间
     * @param start    开始时间
     * @param end      结束时间
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        if (dateTime == null || start == null || end == null) {
            return false;
        }
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    /**
     * 获取年龄（根据生日计算）
     *
     * @param birthDate 生日
     * @return 年龄
     */
    public static int getAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * 生成订单号（时间戳+随机数）
     *
     * @param prefix 前缀
     * @return 订单号
     */
    public static String generateOrderNo(String prefix) {
        String timePart = LocalDateTime.now().format(DATETIME_FORMATTER_COMPACT);
        String randomPart = StringUtil.randomNumber(6);
        return (StringUtil.isEmpty(prefix) ? "" : prefix) + timePart + randomPart;
    }

    /**
     * 获取本周的开始日期（周一）
     *
     * @return 本周的开始日期
     */
    public static LocalDate getStartOfWeek() {
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.MONDAY);
    }

    /**
     * 获取本周的结束日期（周日）
     *
     * @return 本周的结束日期
     */
    public static LocalDate getEndOfWeek() {
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.SUNDAY);
    }

    /**
     * 获取本月的开始日期
     *
     * @return 本月的开始日期
     */
    public static LocalDate getStartOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * 获取本月的结束日期
     *
     * @return 本月的结束日期
     */
    public static LocalDate getEndOfMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }
}