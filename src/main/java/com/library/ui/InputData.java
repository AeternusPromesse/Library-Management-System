package com.library.ui;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * 输入验证类：实现整数、小数、字符串、一行字符的正常输入，
 * 输入有误时提示并重新输入；手机号输入方法实现手机号格式验证。
 */
public final class InputData {

    /** 手机号格式：1 开头的 11 位数字 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private static final Scanner SC = new Scanner(System.in, StandardCharsets.UTF_8);

    private InputData() {
    }

    /** 读入一行字符并去除首尾空格 */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    /** 读入一个非空字符串 */
    public static String readString(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) {
                return s;
            }
            System.out.println("输入不能为空，请重新输入。");
        }
    }

    /** 读入一个整数，输入有误时提示并重新输入 */
    public static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("输入无效，请输入一个整数。");
            }
        }
    }

    /** 读入一个小数，输入有误时提示并重新输入 */
    public static double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("输入无效，请输入一个数字。");
            }
        }
    }

    /** 读入一个手机号并验证格式，格式错误时提示并重新输入 */
    public static String readPhone(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (PHONE_PATTERN.matcher(s).matches()) {
                return s;
            }
            System.out.println("手机号格式不正确（应为1开头的11位数字），请重新输入。");
        }
    }

    /** 读入一个日期（yyyy-MM-dd），格式错误时提示并重新输入 */
    public static LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readLine(prompt));
            } catch (DateTimeParseException e) {
                System.out.println("日期格式错误，请使用 yyyy-MM-dd 格式。");
            }
        }
    }
}
