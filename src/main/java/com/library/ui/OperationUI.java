package com.library.ui;

import com.library.model.Admin;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Loan;
import com.library.model.Operator;
import com.library.model.Recommendation;
import com.library.model.Student;
import com.library.model.Teacher;
import com.library.model.User;
import com.library.service.BookManager;
import com.library.service.LoanManager;
import com.library.service.LogManager;
import com.library.service.RecommendationManager;
import com.library.service.UserManager;
import com.library.util.SysOperationExcept;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 系统操作类：项目启动、注册、登录、按角色显示操作界面并执行相应操作。
 */
public class OperationUI {

    /** 专业选项 */
    static final String[] MAJORS = {
            "计算机科学与技术", "软件工程", "电子信息工程",
            "汉语言文学", "历史学", "哲学", "艺术设计"
    };

    /** 角色选项 */
    static final String[] ROLE_TYPE = {
            User.ROLE_ADMIN, User.ROLE_STUDENT, User.ROLE_TEACHER, User.ROLE_OPERATOR
    };

    /** 副本状态选项 */
    static final String[] COPY_STATUS = {
            BookCopy.STATUS_NORMAL, BookCopy.STATUS_DAMAGED, BookCopy.STATUS_DISCARDED
    };

    private static LogManager logManager;
    private static UserManager userManager;
    private static BookManager bookManager;
    private static LoanManager loanManager;
    private static RecommendationManager recommendationManager;
    private static User currentUser;

    private OperationUI() {
    }

    /** 项目启动方法：1、注册；2、登陆；3、退出 */
    public static void start() {
        logManager = new LogManager();
        userManager = new UserManager(logManager);
        bookManager = new BookManager();
        loanManager = new LoanManager(userManager, bookManager);
        recommendationManager = new RecommendationManager();

        while (true) {
            System.out.println("\n================ 图书馆管理系统 ================");
            System.out.println("1. 注册");
            System.out.println("2. 登录");
            System.out.println("3. 退出");
            int choice = InputData.readInt("请选择操作: ");
            try {
                switch (choice) {
                    case 1 -> register();
                    case 2 -> login();
                    case 3 -> {
                        System.out.println("感谢使用，再见！");
                        return;
                    }
                    default -> System.out.println("输入无效，请重新选择。");
                }
            } catch (SysOperationExcept e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /** 选项选择：显示选项，并返回选择内容 */
    static String optionSelect(String[] options) {
        return optionSelect("请选择: ", options);
    }

    static String optionSelect(String prompt, String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        while (true) {
            int idx = InputData.readInt(prompt) - 1;
            if (idx >= 0 && idx < options.length) {
                return options[idx];
            }
            System.out.println("选择无效，请重新选择。");
        }
    }

    // ==================== 注册 ====================

    /** 注册操作：输入姓名、密码、手机号（验证），选择专业和角色，按角色输入信息项 */
    private static void register() throws SysOperationExcept {
        System.out.println("\n--- 用户注册 ---");
        String username = InputData.readString("请输入用户名: ");
        String password = InputData.readString("请输入密码: ");
        String phone = InputData.readPhone("请输入手机号: ");
        System.out.println("请选择专业:");
        String major = optionSelect(MAJORS);
        System.out.println("请选择角色:");
        String role = optionSelect(ROLE_TYPE);

        User user;
        switch (role) {
            case User.ROLE_ADMIN -> user = new Admin(0, username, password, phone, major,
                    InputData.readInt("请输入权限等级: "),
                    InputData.readDate("请输入入职时间(yyyy-MM-dd): "));
            case User.ROLE_STUDENT -> user = new Student(0, username, password, phone, major,
                    InputData.readString("请输入年级: "),
                    InputData.readString("请输入所属学院: "));
            case User.ROLE_TEACHER -> user = new Teacher(0, username, password, phone, major,
                    InputData.readString("请输入所属学院: "),
                    InputData.readString("请输入职称: "));
            default -> user = new Operator(0, username, password, phone, major,
                    InputData.readString("请输入负责区域: "),
                    InputData.readString("请输入职务: "));
        }

        Map<String, Object> result = userManager.addUser(user);
        if ((int) result.get("code") != 200) {
            throw new SysOperationExcept((String) result.get("message"));
        }
        System.out.println("注册成功，请登录。");
    }

    // ==================== 登录 ====================

    /** 登录操作：输入用户名、密码，选择角色，登录成功后写入日志并显示角色菜单 */
    private static void login() throws SysOperationExcept {
        System.out.println("\n--- 用户登录 ---");
        String username = InputData.readString("请输入用户名: ");
        String password = InputData.readString("请输入密码: ");
        System.out.println("请选择角色:");
        String role = optionSelect(ROLE_TYPE);

        Map<String, Object> result = userManager.loginCheck(username, password);
        if ((int) result.get("code") != 200) {
            throw new SysOperationExcept((String) result.get("message"));
        }
        User user = (User) result.get("user");
        if (!user.getRole().equals(role)) {
            userManager.logout();
            throw new SysOperationExcept("登录失败：角色选择与账户不匹配。");
        }
        logManager.log("[" + user.getUsername() + "] 登录系统");
        System.out.println("登录成功，欢迎您，" + user);
        showRoleMenuAndExecute(user);
    }

    /** 根据用户角色显示不同的操作界面，并实现选择菜单 */
    static void showRoleMenuAndExecute(User user) {
        currentUser = user;
        String[] menu = switch (user.getRole()) {
            case User.ROLE_ADMIN -> new String[]{
                    "用户信息查询", "借阅记录查询", "借阅数据统计", "借书", "还书", "审阅推荐图书"};
            case User.ROLE_OPERATOR -> new String[]{
                    "添加书籍", "查看所有书籍", "废弃图书", "编辑图书", "图书副本编辑",
                    "借书", "还书", "个人借阅查询"};
            case User.ROLE_TEACHER -> new String[]{
                    "借书", "还书", "个人借阅查询", "推荐图书"};
            default -> new String[]{"借书", "还书", "个人借阅查询"}; // student
        };
        while (true) {
            System.out.println("\n=============== " + user.getUsername() + " 的操作界面 ===============");
            for (int i = 0; i < menu.length; i++) {
                System.out.println((i + 1) + ". " + menu[i]);
            }
            System.out.println("0. 退出登录");
            int choice = InputData.readInt("请选择操作: ");
            if (choice == 0) {
                logManager.log("[" + user.getUsername() + "] 退出登录");
                userManager.logout();
                currentUser = null;
                System.out.println("已退出登录。");
                return;
            }
            if (choice < 1 || choice > menu.length) {
                System.out.println("输入无效，请重新选择。");
                continue;
            }
            executePermission(menu[choice - 1]);
        }
    }

    /** 根据用户选择的菜单执行相应操作 */
    static void executePermission(String permission) {
        switch (permission) {
            case "用户信息查询" -> searchUsersUI();
            case "借阅记录查询" -> searchLoansUI();
            case "借阅数据统计" -> statisticsUI();
            case "借书" -> borrowUI();
            case "还书" -> returnUI();
            case "审阅推荐图书" -> reviewRecommendUI();
            case "添加书籍" -> addBookUI();
            case "查看所有书籍" -> searchBooksUI();
            case "废弃图书" -> discardBookUI();
            case "编辑图书" -> editBookUI();
            case "图书副本编辑" -> editCopiesUI();
            case "个人借阅查询" -> myLoansUI();
            case "推荐图书" -> recommendUI();
            default -> System.out.println("该功能暂未开放。");
        }
    }

    // ==================== 图书功能（Operator） ====================

    /** 添加图书：反复添加直到退出，添加前显示图书列表，新图书显示在列表第一行 */
    private static void addBookUI() {
        while (true) {
            System.out.println("\n--- 添加图书 ---");
            System.out.println("当前图书列表：");
            printBooks(bookManager.searchBooks(""));
            String title = InputData.readLine("请输入书名(输入空行退出添加): ");
            if (title.isEmpty()) {
                System.out.println("退出添加图书。");
                return;
            }
            String author = InputData.readString("请输入作者: ");
            System.out.println("请选择图书类型:");
            String type = optionSelect(Book.BOOK_TYPES);
            String publisher = InputData.readString("请输入出版社: ");
            int days = InputData.readInt("请输入还书期限(天): ");
            double price = InputData.readDouble("请输入价格(元): ");
            int stock = InputData.readInt("请输入库存数量: ");
            if (stock <= 0) {
                System.out.println("库存数量必须大于0，本次添加取消。");
                continue;
            }
            Book book = bookManager.addBook(title, author, type, publisher, days, price, stock);
            System.out.println("添加成功: " + book);
            logManager.log("[" + currentUser.getUsername() + "] 添加图书《" + title + "》");
            System.out.println("\n添加后的图书列表（新图书在第一行）：");
            printBooks(bookManager.searchBooks(""));
        }
    }

    /** 废弃图书：全部副本归还后才能废弃 */
    private static void discardBookUI() {
        System.out.println("\n--- 废弃图书 ---");
        printBooks(bookManager.searchBooks(""));
        int bookId = InputData.readInt("请输入要废弃的图书ID(0取消): ");
        if (bookId == 0) {
            return;
        }
        String msg = bookManager.discardBook(bookId);
        System.out.println(msg);
        if (msg.startsWith("废弃成功")) {
            Book book = bookManager.findBook(bookId);
            logManager.log("[" + currentUser.getUsername() + "] 废弃图书《" + book.getTitle() + "》");
        }
    }

    /** 编辑图书：只更新书名、作者、类型、出版社、还书期限、价格 */
    private static void editBookUI() {
        System.out.println("\n--- 编辑图书 ---");
        printBooks(bookManager.searchBooks(""));
        int bookId = InputData.readInt("请输入要编辑的图书ID(0取消): ");
        if (bookId == 0) {
            return;
        }
        Book old = bookManager.findBook(bookId);
        if (old == null) {
            System.out.println("图书不存在。");
            return;
        }
        String title = InputData.readString("请输入新书名(" + old.getTitle() + "): ");
        String author = InputData.readString("请输入新作者(" + old.getAuthor() + "): ");
        System.out.println("请选择图书类型:");
        String type = optionSelect(Book.BOOK_TYPES);
        String publisher = InputData.readString("请输入新出版社(" + old.getPublisher() + "): ");
        int days = InputData.readInt("请输入还书期限(天)(" + old.getDays() + "): ");
        double price = InputData.readDouble("请输入价格(元)(" + old.getPrice() + "): ");
        Book book = bookManager.updateBook(bookId, title, author, type, publisher, days, price);
        if (book == null) {
            System.out.println("修改失败：图书不存在。");
        } else {
            System.out.println("修改成功: " + book);
            logManager.log("[" + currentUser.getUsername() + "] 编辑图书《" + book.getTitle() + "》");
        }
    }

    /** 图书副本编辑：新增副本（库存加1）、修改副本状态（正常/损坏/废弃） */
    private static void editCopiesUI() {
        System.out.println("\n--- 图书副本编辑 ---");
        System.out.println("正常图书列表：");
        printBooks(bookManager.getNormalBooks());
        int bookId = InputData.readInt("请输入图书ID进入副本编辑(0取消): ");
        if (bookId == 0) {
            return;
        }
        Book book = bookManager.findBook(bookId);
        if (book == null) {
            System.out.println("图书不存在。");
            return;
        }
        while (true) {
            System.out.println("\n《" + book.getTitle() + "》副本列表 (库存:" + book.getStock() + ")");
            for (BookCopy c : book.getBookCopies()) {
                System.out.println("  " + c);
            }
            System.out.println("1. 新增副本");
            System.out.println("2. 修改副本状态");
            System.out.println("0. 返回");
            int choice = InputData.readInt("请选择: ");
            switch (choice) {
                case 1 -> {
                    BookCopy copy = bookManager.addCopy(bookId);
                    if (copy == null) {
                        System.out.println("新增失败：图书不存在或已废弃。");
                    } else {
                        System.out.println("新增成功: " + copy + "，当前库存:" + bookManager.findBook(bookId).getStock());
                        logManager.log("[" + currentUser.getUsername() + "] 为《" + book.getTitle() + "》新增副本#" + copy.getCopyId());
                    }
                }
                case 2 -> {
                    int copyId = InputData.readInt("请输入副本ID: ");
                    System.out.println("请选择新状态:");
                    String status = optionSelect(COPY_STATUS);
                    String msg = bookManager.changeCopyStatus(bookId, copyId, status);
                    System.out.println(msg);
                    if (msg.startsWith("修改成功")) {
                        logManager.log("[" + currentUser.getUsername() + "] 修改《" + book.getTitle() + "》副本#" + copyId + " 状态为" + status);
                    }
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("输入无效。");
            }
        }
    }

    // ==================== 借书与还书 ====================

    /** 借书：显示个人借阅记录与可借图书列表，按图书ID借阅，可反复借书 */
    private static void borrowUI() {
        while (true) {
            System.out.println("\n--- 我的未还借阅 ---");
            printLoans(loanManager.getUnreturnedLoansByUser(currentUser.getId()));
            System.out.println("\n--- 可借图书列表 ---");
            List<Book> available = bookManager.searchBooks("").stream()
                    .filter(b -> Book.STATUS_NORMAL.equals(b.getStatus()) && b.countAvailable() > 0)
                    .toList();
            printBooks(available);
            int bookId = InputData.readInt("请输入要借阅的图书ID(输入0退出借书): ");
            if (bookId == 0) {
                System.out.println("退出借书。");
                return;
            }
            Loan loan = loanManager.borrowBook(currentUser.getId(), bookId);
            if (loan == null) {
                System.out.println("借阅失败：图书不存在、已废弃或无可用副本。");
            } else {
                System.out.println("借阅成功: " + loanManager.describeLoan(loan));
                Book b = bookManager.findBook(bookId);
                logManager.log("[" + currentUser.getUsername() + "] 借阅《" + b.getTitle() + "》(副本#" + loan.getCopyId() + ")");
                System.out.println("\n我的未还借阅：");
                printLoans(loanManager.getUnreturnedLoansByUser(currentUser.getId()));
            }
        }
    }

    /** 还书：显示个人借阅记录，按副本ID归还，可反复还书 */
    private static void returnUI() {
        while (true) {
            System.out.println("\n--- 我的未还借阅 ---");
            printLoans(loanManager.getUnreturnedLoansByUser(currentUser.getId()));
            int copyId = InputData.readInt("请输入要归还的副本ID(输入0退出还书): ");
            if (copyId == 0) {
                System.out.println("退出还书。");
                return;
            }
            Loan loan = loanManager.returnBook(currentUser.getId(), copyId);
            if (loan == null) {
                System.out.println("归还失败：未找到该副本的未还借阅记录。");
            } else {
                System.out.println("归还成功: " + loanManager.describeLoan(loan));
                Book b = bookManager.findBook(loan.getBookId());
                String fineInfo = loan.getFine() > 0 ? "，逾期罚款" + loan.getFine() + "元" : "";
                logManager.log("[" + currentUser.getUsername() + "] 归还《" + (b == null ? "未知图书" : b.getTitle())
                        + "》(副本#" + copyId + ")" + fineInfo);
            }
        }
    }

    // ==================== 查询与统计 ====================

    /** 用户信息查询：模糊匹配用户名与手机号，直接回车显示全部 */
    private static void searchUsersUI() {
        String keyword = InputData.readLine("请输入用户名/手机号关键字(直接回车显示全部): ");
        printUsers(userManager.searchUsers(keyword));
    }

    /** 查询书籍：关键字模糊查询，直接回车显示全部图书 */
    private static void searchBooksUI() {
        String keyword = InputData.readLine("请输入书名/作者/类型/出版社关键字(直接回车显示全部): ");
        printBooks(bookManager.searchBooks(keyword));
    }

    /** 借阅记录查询：1关键字查询 2超期借阅 3日期范围查询 */
    private static void searchLoansUI() {
        while (true) {
            System.out.println("\n--- 借阅记录查询 ---");
            System.out.println("1. 关键字查询");
            System.out.println("2. 超期借阅查询");
            System.out.println("3. 日期范围查询");
            System.out.println("0. 返回上级");
            int choice = InputData.readInt("请选择: ");
            switch (choice) {
                case 1 -> {
                    String kw = InputData.readLine("请输入关键字(用户名/图书名，直接回车显示全部): ");
                    printLoans(loanManager.searchLoans(kw));
                }
                case 2 -> {
                    String kw = InputData.readLine("请输入关键字(用户名/图书名，直接回车显示全部超期): ");
                    printLoans(loanManager.searchOverdueLoans(kw));
                }
                case 3 -> {
                    LocalDate start = InputData.readDate("请输入开始日期(yyyy-MM-dd): ");
                    LocalDate end = InputData.readDate("请输入结束日期(yyyy-MM-dd): ");
                    printLoans(loanManager.searchLoansByDateRange(start, end));
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("输入无效。");
            }
        }
    }

    /** 借阅数据统计：按图书类型、按用户专业归集统计借阅次数 */
    private static void statisticsUI() {
        System.out.println("\n===== 按图书类型统计借阅次数 =====");
        for (Map.Entry<String, Integer> e : loanManager.statisticsByBookType().entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue() + "次");
        }
        System.out.println("\n===== 按用户专业统计借阅次数 =====");
        for (Map.Entry<String, Integer> e : loanManager.statisticsByMajor().entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue() + "次");
        }
    }

    /** 个人借阅查询：全部借阅、未还借阅、日期范围查询 */
    private static void myLoansUI() {
        while (true) {
            System.out.println("\n--- 个人借阅查询 ---");
            System.out.println("1. 查看全部借阅");
            System.out.println("2. 查看未还借阅");
            System.out.println("3. 日期范围查询");
            System.out.println("0. 返回上级");
            int choice = InputData.readInt("请选择: ");
            switch (choice) {
                case 1 -> printLoans(loanManager.getLoansByUser(currentUser.getId()));
                case 2 -> printLoans(loanManager.getUnreturnedLoansByUser(currentUser.getId()));
                case 3 -> {
                    LocalDate start = InputData.readDate("请输入开始日期(yyyy-MM-dd): ");
                    LocalDate end = InputData.readDate("请输入结束日期(yyyy-MM-dd): ");
                    printLoans(loanManager.searchLoansByDateRange(currentUser.getId(), start, end));
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("输入无效。");
            }
        }
    }

    // ==================== 推荐图书 ====================

    /** 推荐图书（教师） */
    private static void recommendUI() {
        System.out.println("\n--- 推荐图书 ---");
        String title = InputData.readString("请输入推荐书名: ");
        String author = InputData.readString("请输入作者: ");
        Recommendation r = recommendationManager.add(title, author, currentUser);
        System.out.println("推荐成功: " + r);
        logManager.log("[" + currentUser.getUsername() + "] 推荐图书《" + title + "》");
    }

    /** 审阅推荐图书（管理员） */
    private static void reviewRecommendUI() {
        System.out.println("\n--- 审阅推荐图书 ---");
        List<Recommendation> all = recommendationManager.getAll();
        if (all.isEmpty()) {
            System.out.println("(暂无推荐记录)");
            return;
        }
        for (Recommendation r : all) {
            System.out.println(r);
        }
        int id = InputData.readInt("请输入要审阅的推荐ID(0取消): ");
        if (id == 0) {
            return;
        }
        System.out.println("请选择审阅结果:");
        String result = optionSelect(new String[]{"通过", "驳回"});
        String msg = recommendationManager.review(id, "通过".equals(result));
        System.out.println(msg);
        if (msg.startsWith("审阅完成")) {
            logManager.log("[" + currentUser.getUsername() + "] 审阅推荐#" + id + "：" + result);
        }
    }

    // ==================== 打印辅助 ====================

    private static void printBooks(List<Book> list) {
        if (list.isEmpty()) {
            System.out.println("(无图书)");
            return;
        }
        for (Book b : list) {
            System.out.println(b);
        }
    }

    private static void printUsers(List<User> list) {
        if (list.isEmpty()) {
            System.out.println("(无用户)");
            return;
        }
        for (User u : list) {
            System.out.println(u);
        }
    }

    private static void printLoans(List<Loan> list) {
        if (list.isEmpty()) {
            System.out.println("(无借阅记录)");
            return;
        }
        for (Loan l : list) {
            System.out.println(loanManager.describeLoan(l));
        }
    }
}
