package com.library.ui;

import java.util.Scanner;

/**
 * 控制台输入工具类。
 */
public final class ConsoleUtil {

    private ConsoleUtil() {
    }

    public static String readLine(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static int readInt(Scanner sc, String prompt) {
        while (true) {
            String s = readLine(sc, prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("输入无效，请输入一个整数。");
            }
        }
    }
}