package com.library.service;

import com.google.gson.reflect.TypeToken;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Loan;
import com.library.model.User;
import com.library.util.JsonUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 借阅记录数据管理类：借书、还书、各类查询与统计，并持久化到 JSON 文件。
 */
public class LoanManager {

    private final Path loansFile;
    private final UserManager userManager;
    private final BookManager bookManager;

    private List<Loan> loans = new ArrayList<>();
    private int nextLoanId = 1;

    public LoanManager(UserManager userManager, BookManager bookManager) {
        this.userManager = userManager;
        this.bookManager = bookManager;
        loansFile = Paths.get(System.getProperty("user.dir"), "data", "loans.json");
        loans = JsonUtil.loadList(loansFile, new TypeToken<List<Loan>>() {}.getType());
        if (loans.isEmpty()) {
            loans = initDemoLoans();
            save();
        }
        nextLoanId = loans.stream().mapToInt(Loan::getId).max().orElse(0) + 1;
    }

    /**
     * 借书：校验图书与可借副本（状态正常且未借出），副本借出后生成借阅记录，
     * 应还日期 = 借阅日期 + 图书的还书期限 days。
     */
    public Loan borrowBook(int userId, int bookId) {
        Book book = bookManager.findBook(bookId);
        if (book == null || !Book.STATUS_NORMAL.equals(book.getStatus())) {
            return null;
        }
        BookCopy copy = book.getAvailableCopy();
        if (copy == null) {
            return null;
        }
        copy.borrow();
        copy.update();
        book.update();
        LocalDate today = LocalDate.now();
        Loan loan = new Loan(nextLoanId++, bookId, userId, copy.getCopyId(),
                today, today.plusDays(book.getDays()));
        loans.add(loan);
        save();
        bookManager.save();
        return loan;
    }

    /**
     * 还书：通过副本ID查找该用户的未还借阅记录，执行归还并计算逾期罚款。
     */
    public Loan returnBook(int userId, int copyId) {
        Loan loan = loans.stream()
                .filter(l -> l.getUserId() == userId && l.getCopyId() == copyId && !l.isReturned())
                .findFirst().orElse(null);
        if (loan == null) {
            return null;
        }
        loan.returnBook(); // 设置归还日期并计算逾期罚款
        Book book = bookManager.findBook(loan.getBookId());
        if (book != null) {
            if (book.getBookCopies() != null) {
                book.getBookCopies().stream()
                        .filter(c -> c.getCopyId() == copyId)
                        .findFirst()
                        .ifPresent(c -> {
                            c.returnCopy();
                            c.update();
                        });
            }
            book.update();
            bookManager.save();
        }
        save();
        return loan;
    }

    // ==================== 查询 ====================

    /** 个人全部借阅 */
    public List<Loan> getLoansByUser(int userId) {
        return loans.stream().filter(l -> l.getUserId() == userId)
                .collect(Collectors.toList());
    }

    /** 个人未还借阅 */
    public List<Loan> getUnreturnedLoansByUser(int userId) {
        return loans.stream()
                .filter(l -> l.getUserId() == userId && !l.isReturned())
                .collect(Collectors.toList());
    }

    /** 关键字查询：模糊匹配用户名与图书名（关键字为空返回全部） */
    public List<Loan> searchLoans(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>(loans);
        }
        return loans.stream()
                .filter(l -> matchKeyword(l, keyword))
                .collect(Collectors.toList());
    }

    /** 超期查询：在未还且超期的记录中按关键字（用户名/图书名）模糊查询 */
    public List<Loan> searchOverdueLoans(String keyword) {
        LocalDate today = LocalDate.now();
        return loans.stream()
                .filter(l -> !l.isReturned() && l.getDueDate() != null && l.getDueDate().isBefore(today))
                .filter(l -> keyword == null || keyword.isEmpty() || matchKeyword(l, keyword))
                .collect(Collectors.toList());
    }

    /** 日期范围查询：按借阅日期查询 */
    public List<Loan> searchLoansByDateRange(LocalDate start, LocalDate end) {
        return loans.stream()
                .filter(l -> l.getBorrowDate() != null
                        && !l.getBorrowDate().isBefore(start) && !l.getBorrowDate().isAfter(end))
                .collect(Collectors.toList());
    }

    /** 个人借阅的日期范围查询 */
    public List<Loan> searchLoansByDateRange(int userId, LocalDate start, LocalDate end) {
        return loans.stream()
                .filter(l -> l.getUserId() == userId
                        && l.getBorrowDate() != null
                        && !l.getBorrowDate().isBefore(start) && !l.getBorrowDate().isAfter(end))
                .collect(Collectors.toList());
    }

    /** 关键字是否命中借阅记录（用户名或图书名） */
    private boolean matchKeyword(Loan l, String keyword) {
        String k = keyword.toLowerCase();
        User u = userManager.findUser(l.getUserId());
        Book b = bookManager.findBook(l.getBookId());
        return String.valueOf(l.getId()).contains(k)
                || (u != null && u.getUsername().toLowerCase().contains(k))
                || (b != null && b.getTitle().toLowerCase().contains(k))
                || String.valueOf(l.getBookId()).contains(k)
                || String.valueOf(l.getUserId()).contains(k);
    }

    // ==================== 统计 ====================

    /** 按图书类型统计借阅次数：7 种类型全部显示，按次数降序 */
    public Map<String, Integer> statisticsByBookType() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String type : Book.BOOK_TYPES) {
            counts.put(type, 0);
        }
        for (Loan l : loans) {
            Book b = bookManager.findBook(l.getBookId());
            if (b != null) {
                counts.merge(b.getType(), 1, Integer::sum);
            }
        }
        return sortByValueDesc(counts);
    }

    /** 按用户专业统计借阅次数，按次数降序 */
    public Map<String, Integer> statisticsByMajor() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Loan l : loans) {
            User u = userManager.findUser(l.getUserId());
            if (u != null) {
                String major = (u.getMajor() == null || u.getMajor().isEmpty()) ? "未填写专业" : u.getMajor();
                counts.merge(major, 1, Integer::sum);
            }
        }
        return sortByValueDesc(counts);
    }

    private Map<String, Integer> sortByValueDesc(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    // ==================== 展示辅助 ====================

    /** 生成借阅记录的描述信息 */
    public String describeLoan(Loan l) {
        Book b = bookManager.findBook(l.getBookId());
        User u = userManager.findUser(l.getUserId());
        String book = b == null ? "未知图书(ID:" + l.getBookId() + ")" : b.getTitle() + "(ID:" + b.getId() + ")";
        String user = u == null ? "未知用户(ID:" + l.getUserId() + ")" : u.getUsername() + "(ID:" + u.getId() + ")";
        String status;
        if (l.isReturned()) {
            status = "已归还(" + l.getReturnDate()
                    + (l.getFine() > 0 ? "，罚款" + l.getFine() + "元" : "，无罚款") + ")";
        } else if (l.isOverdue()) {
            status = "超期未还";
        } else {
            status = "借阅中";
        }
        return String.format("借阅ID:%d | 图书:%s | 副本ID:%d | 用户:%s | 借阅日期:%s | 应还日期:%s | 状态:%s",
                l.getId(), book, l.getCopyId(), user, l.getBorrowDate(), l.getDueDate(), status);
    }

    public void save() {
        JsonUtil.save(loansFile, loans);
    }

    // ==================== 初始化演示数据 ====================

    /**
     * 初始化演示借阅记录：包含借阅中、超期未还、已归还（含逾期罚款）的记录，便于查询与统计演示。
     */
    private List<Loan> initDemoLoans() {
        User student1 = userManager.findUserByName("student1");
        User student2 = userManager.findUserByName("student2");
        User teacher = userManager.findUserByName("teacher");
        if (student1 == null || student2 == null || teacher == null || bookManager.findBook(1) == null) {
            return new ArrayList<>();
        }
        LocalDate today = LocalDate.now();
        List<Loan> list = new ArrayList<>();
        // 超期未还：借出40天（期限30天）
        list.add(new Loan(1, 1, student1.getId(), 1, today.minusDays(40), today.minusDays(40).plusDays(30)));
        // 借阅中：借出5天（期限15天）
        list.add(new Loan(2, 2, student1.getId(), 6, today.minusDays(5), today.minusDays(5).plusDays(15)));
        // 已归还、无罚款
        Loan l3 = new Loan(3, 7, teacher.getId(), 31, today.minusDays(20), today.minusDays(20).plusDays(30));
        l3.setReturnDate(today.minusDays(18));
        list.add(l3);
        // 已归还、逾期5天，罚款2.5元
        Loan l4 = new Loan(4, 4, student2.getId(), 16, today.minusDays(45), today.minusDays(45).plusDays(30));
        l4.setReturnDate(today.minusDays(10));
        l4.setFine(Loan.calcOverdueFee(l4.getDueDate(), l4.getReturnDate()));
        list.add(l4);
        // 借阅中：昨天借出
        list.add(new Loan(5, 3, teacher.getId(), 11, today.minusDays(1), today.minusDays(1).plusDays(30)));
        // 标记对应副本为已借出
        markCopyBorrowed(1, 1, true);
        markCopyBorrowed(2, 6, true);
        markCopyBorrowed(3, 11, true);
        bookManager.save();
        return list;
    }

    private void markCopyBorrowed(int bookId, int copyId, boolean borrowed) {
        Book book = bookManager.findBook(bookId);
        if (book == null || book.getBookCopies() == null) {
            return;
        }
        book.getBookCopies().stream()
                .filter(c -> c.getCopyId() == copyId)
                .findFirst()
                .ifPresent(c -> c.setBorrowed(borrowed));
    }
}
