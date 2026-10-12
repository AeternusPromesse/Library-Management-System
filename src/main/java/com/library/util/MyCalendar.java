package com.library.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日历类：基于 LocalDateTime 实现“指定日期（或当前日期）+ 当月日历”的打印。
 */
public class MyCalendar {

    /** 一周表头（周一开头，周日结尾） */
    private static final String[] WEEK_HEADER = {"一", "二", "三", "四", "五", "六", "日"};

    private final LocalDateTime dateTime;
    private final int year;
    private final int month;
    private final int day;

    /** 无参构造：表示当前日期 */
    public MyCalendar() {
        this(LocalDateTime.now());
    }

    /** 有参构造：表示指定日期 */
    public MyCalendar(int year, int month, int day) {
        this(LocalDateTime.of(year, month, day, 0, 0));
    }

    private MyCalendar(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        this.year = dateTime.getYear();
        this.month = dateTime.getMonthValue();
        this.day = dateTime.getDayOfMonth();
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // 标题：--------2026年3月24日--------
        sb.append("--------").append(year).append("年").append(month)
                .append("月").append(day).append("日").append("--------\n");

        // 星期表头（周一开始）
        for (String w : WEEK_HEADER) {
            sb.append(String.format("%3s", w));
        }
        sb.append("\n");

        // 当月 1 号对应的列偏移（周一位于第 0 列）
        LocalDate firstDay = dateTime.toLocalDate().withDayOfMonth(1);
        int offset = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = dateTime.toLocalDate().lengthOfMonth();

        for (int i = 0; i < offset; i++) {
            sb.append("   ");
        }
        for (int d = 1; d <= daysInMonth; d++) {
            sb.append(String.format("%3d", d));
            if ((offset + d) % 7 == 0) {
                sb.append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}