package com.library.ui;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.service.LibraryService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 管理员操作界面。
 */
public class AdminMenu {

    private final LibraryService service;
    private final Scanner sc;

    public AdminMenu(LibraryService service, Scanner sc) {
        this.service = service;
        this.sc = sc;
    }

    public void run() {
        while (true) {
            System.out.println("\n================ 管理员操作界面 ================");
            System.out.println("1. 添加图书");
            System.out.println("2. 查询图书");
            System.out.println("3. 查询用户");
            System.out.println("4. 查询借阅信息");
            System.out.println("5. 统计数据");
            System.out.println("0. 退出登录");
            int choice = ConsoleUtil.readInt(sc, "请选择操作: ");

            switch (choice) {
                case 1 -> addBook();
                case 2 -> searchBooks();
                case 3 -> searchUsers();
                case 4 -> searchLoans();
                case 5 -> statistics();
                case 0 -> {
                    System.out.println("已退出登录。");
                    return;
                }
                default -> System.out.println("输入无效，请重新选择。");
            }
        }
    }

    private void addBook() {
        while (true) {
            System.out.println("\n--- 添加图书 ---");
            String title = ConsoleUtil.readLine(sc, "请输入书名(输入空行停止): ");
            if (title.isEmpty()) {
                break;
            }
            String author = ConsoleUtil.readLine(sc, "请输入作者: ");
            String type = selectBookType();
            int total = ConsoleUtil.readInt(sc, "请输入库存数量: ");
            if (total <= 0) {
                System.out.println("库存数量必须大于0。");
                continue;
            }
            Book book = service.addBook(title, author, type, total);
            System.out.println("添加成功: " + book);
            printBooks(service.getAllBooks());
        }
    }

    private String selectBookType() {
        System.out.println("可选择图书类型: ");
        for (int i = 0; i < Book.BOOK_TYPES.length; i++) {
            System.out.println("  " + (i + 1) + ". " + Book.BOOK_TYPES[i]);
        }
        int idx = ConsoleUtil.readInt(sc, "请选择类型(1-" + Book.BOOK_TYPES.length + "): ") - 1;
        if (idx < 0 || idx >= Book.BOOK_TYPES.length) {
            System.out.println("类型选择无效，默认使用「" + Book.BOOK_TYPES[0] + "」。");
            return Book.BOOK_TYPES[0];
        }
        return Book.BOOK_TYPES[idx];
    }

    private void searchBooks() {
        String keyword = ConsoleUtil.readLine(sc, "请输入书名/作者等关键字(不输入直接回车显示全部): ");
        printBooks(service.searchBooks(keyword));
    }

    private void searchUsers() {
        String keyword = ConsoleUtil.readLine(sc, "请输入用户名/专业等关键字(不输入直接回车显示全部): ");
        printUsers(service.searchUsers(keyword));
    }

    private void searchLoans() {
        while (true) {
            System.out.println("\n--- 查询借阅信息 ---");
            System.out.println("1. 关键字查询");
            System.out.println("2. 查询超期借阅");
            System.out.println("3. 日期范围查询");
            System.out.println("0. 返回上级");
            int choice = ConsoleUtil.readInt(sc, "请选择: ");
            switch (choice) {
                case 1 -> {
                    String kw = ConsoleUtil.readLine(sc, "请输入关键字(图书/用户相关): ");
                    printLoans(service.searchLoans(kw));
                }
                case 2 -> printLoans(service.getOverdueLoans());
                case 3 -> queryByDateRange();
                case 0 -> {
                    return;
                }
                default -> System.out.println("输入无效。");
            }
        }
    }

    private void queryByDateRange() {
        LocalDate start = readDate("请输入开始日期(yyyy-MM-dd): ");
        LocalDate end = readDate("请输入结束日期(yyyy-MM-dd): ");
        if (start == null || end == null) {
            System.out.println("日期格式错误。");
            return;
        }
        printLoans(service.searchLoansByDateRange(start, end));
    }

    private LocalDate readDate(String prompt) {
        String s = ConsoleUtil.readLine(sc, prompt);
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void statistics() {
        System.out.println("\n===== 按图书类型统计借阅总次数 =====");
        Map<String, Integer> byType = service.statisticsByBookType();
        for (Map.Entry<String, Integer> e : byType.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue() + "次");
        }

        System.out.println("\n===== 按用户专业统计借阅总次数 =====");
        Map<String, Integer> byMajor = service.statisticsByMajor();
        for (Map.Entry<String, Integer> e : byMajor.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue() + "次");
        }
    }

    private void printBooks(List<Book> list) {
        if (list.isEmpty()) {
            System.out.println("(无图书)");
            return;
        }
        for (Book b : list) {
            System.out.println(b);
        }
    }

    private void printUsers(List<User> list) {
        if (list.isEmpty()) {
            System.out.println("(无用户)");
            return;
        }
        for (User u : list) {
            System.out.println(u);
        }
    }

    private void printLoans(List<Loan> list) {
        if (list.isEmpty()) {
            System.out.println("(无借阅记录)");
            return;
        }
        for (Loan l : list) {
            System.out.println(service.describeLoan(l));
        }
    }
}