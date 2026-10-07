package com.library;

import com.library.model.User;
import com.library.service.LibraryService;
import com.library.ui.AdminMenu;
import com.library.ui.BorrowerMenu;
import com.library.ui.ConsoleUtil;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 图书馆管理系统入口：负责注册、登录以及按角色分发到不同操作界面。
 */
public class Main {

    public static void main(String[] args) {
        LibraryService service = new LibraryService();
        Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);

        while (true) {
            System.out.println("\n================ 图书馆管理系统 ================");
            System.out.println("1. 登录");
            System.out.println("2. 注册");
            System.out.println("0. 退出系统");
            int choice = ConsoleUtil.readInt(sc, "请选择操作: ");

            switch (choice) {
                case 1 -> {
                    User user = doLogin(service, sc);
                    if (user != null) {
                        dispatch(service, user, sc);
                    }
                }
                case 2 -> doRegister(service, sc);
                case 0 -> {
                    System.out.println("感谢使用，再见！");
                    service.saveAll();
                    return;
                }
                default -> System.out.println("输入无效，请重新选择。");
            }
        }
    }

    private static User doLogin(LibraryService service, Scanner sc) {
        String username = ConsoleUtil.readLine(sc, "请输入用户名: ");
        String password = ConsoleUtil.readLine(sc, "请输入密码: ");
        User user = service.login(username, password);
        if (user == null) {
            System.out.println("登录失败：用户名或密码错误。");
        } else {
            System.out.println("登录成功，欢迎您，" + user.getUsername() + "（" + user.getRole().getLabel() + "）");
        }
        return user;
    }

    private static void doRegister(LibraryService service, Scanner sc) {
        String username = ConsoleUtil.readLine(sc, "请输入用户名: ");
        String password = ConsoleUtil.readLine(sc, "请输入密码: ");
        System.out.println("请选择角色: 1. 管理员  2. 借阅用户");
        int roleChoice = ConsoleUtil.readInt(sc, "请选择(1/2): ");
        User.Role role = roleChoice == 1 ? User.Role.ADMIN : User.Role.BORROWER;
        String major = "";
        if (role == User.Role.BORROWER) {
            major = ConsoleUtil.readLine(sc, "请输入专业（用于统计）: ");
        }
        User user = service.register(username, password, role, major);
        if (user == null) {
            System.out.println("注册失败：用户名已存在。");
        } else {
            System.out.println("注册成功，请登录。");
        }
    }

    private static void dispatch(LibraryService service, User user, Scanner sc) {
        if (user.getRole() == User.Role.ADMIN) {
            new AdminMenu(service, sc).run();
        } else {
            new BorrowerMenu(service, user, sc).run();
        }
    }
}