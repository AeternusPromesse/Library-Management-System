package com.library.ui;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.service.LibraryService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * 借阅用户操作界面。
 */
public class BorrowerMenu {

    private final LibraryService service;
    private final User user;
    private final Scanner sc;

    public BorrowerMenu(LibraryService service, User user, Scanner sc) {
        this.service = service;
        this.user = user;
        this.sc = sc;
    }

    public void run() {
        while (true) {
            System.out.println("\n================ 借阅用户操作界面 ================");
            System.out.println("1. 借书");
            System.out.println("2. 还书");
            System.out.println("3. 查询个人借阅信息");
            System.out.println("0. 退出登录");
            int choice = ConsoleUtil.readInt(sc, "请选择操作: ");

            switch (choice) {
                case 1 -> borrowBooks();
                case 2 -> returnBooks();
                case 3 -> myLoans();
                case 0 -> {
                    System.out.println("已退出登录。");
                    return;
                }
                default -> System.out.println("输入无效，请重新选择。");
            }
        }
    }

    private void borrowBooks() {
        while (true) {
            System.out.println("\n--- 我的未还借阅 ---");
            printLoans(service.getUnreturnedLoansByUser(user.getId()));

            System.out.println("\n--- 可借图书列表 ---");
            printBooks(service.getAvailableBooks());

            int bookId = ConsoleUtil.readInt(sc, "请输入要借阅的图书ID(输入0退出借阅): ");
            if (bookId == 0) {
                System.out.println("退出借阅。");
                return;
            }
            Loan loan = service.borrowBook(user.getId(), bookId);
            if (loan == null) {
                System.out.println("借阅失败：图书不存在或已无可借库存。");
            } else {
                System.out.println("借阅成功: " + service.describeLoan(loan));
            }
        }
    }

    private void returnBooks() {
        while (true) {
            System.out.println("\n--- 我的未还借阅 ---");
            printLoans(service.getUnreturnedLoansByUser(user.getId()));

            int bookId = ConsoleUtil.readInt(sc, "请输入要归还的图书ID(输入0退出还书): ");
            if (bookId == 0) {
                System.out.println("退出还书。");
                return;
            }
            if (service.returnBook(user.getId(), bookId)) {
                System.out.println("归还成功。");
            } else {
                System.out.println("归还失败：你未借阅该书，或该书已归还。");
            }
        }
    }

    private void myLoans() {
        while (true) {
            System.out.println("\n--- 查询个人借阅 ---");
            System.out.println("1. 查看全部借阅");
            System.out.println("2. 查看未还借阅");
            System.out.println("3. 日期范围查询");
            System.out.println("0. 返回上级");
            int choice = ConsoleUtil.readInt(sc, "请选择: ");
            switch (choice) {
                case 1 -> printLoans(service.getLoansByUser(user.getId()));
                case 2 -> printLoans(service.getUnreturnedLoansByUser(user.getId()));
                case 3 -> queryByDateRange();
                case 0 -> {
                    return;
                }
                default -> System.out.println("输入无效。");
            }
        }
    }

    private void queryByDateRange() {
        String s = ConsoleUtil.readLine(sc, "请输入开始日期(yyyy-MM-dd): ");
        String e = ConsoleUtil.readLine(sc, "请输入结束日期(yyyy-MM-dd): ");
        try {
            LocalDate start = LocalDate.parse(s);
            LocalDate end = LocalDate.parse(e);
            printLoans(service.searchLoansByDateRange(user.getId(), start, end));
        } catch (DateTimeParseException ex) {
            System.out.println("日期格式错误，请使用 yyyy-MM-dd。");
        }
    }

    private void printBooks(List<Book> list) {
        if (list.isEmpty()) {
            System.out.println("(暂无图书)");
            return;
        }
        for (Book b : list) {
            System.out.println(b);
        }
    }

    private void printLoans(List<Loan> list) {
        if (list.isEmpty()) {
            System.out.println("(暂无借阅记录)");
            return;
        }
        for (Loan l : list) {
            System.out.println(service.describeLoan(l));
        }
    }
}