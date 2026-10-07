package com.library.service;

import com.google.gson.reflect.TypeToken;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.util.JsonUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书馆核心业务逻辑：负责数据加载/保存、用户图书借阅的增删改查以及统计分析。
 */
public class LibraryService {

    private final Path dataDir;
    private final Path usersFile;
    private final Path booksFile;
    private final Path loansFile;

    private List<User> users = new ArrayList<>();
    private List<Book> books = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();

    private int nextUserId = 1;
    private int nextBookId = 1;
    private int nextLoanId = 1;

    public LibraryService() {
        dataDir = Paths.get(System.getProperty("user.dir"), "data");
        usersFile = dataDir.resolve("users.json");
        booksFile = dataDir.resolve("books.json");
        loansFile = dataDir.resolve("loans.json");
        load();
    }

    private void load() {
        users = JsonUtil.loadList(usersFile, new TypeToken<List<User>>() {}.getType());
        books = JsonUtil.loadList(booksFile, new TypeToken<List<Book>>() {}.getType());
        loans = JsonUtil.loadList(loansFile, new TypeToken<List<Loan>>() {}.getType());

        nextUserId = users.stream().mapToInt(User::getId).max().orElse(0) + 1;
        nextBookId = books.stream().mapToInt(Book::getId).max().orElse(0) + 1;
        nextLoanId = loans.stream().mapToInt(Loan::getId).max().orElse(0) + 1;

        if (!Files.exists(usersFile) && !Files.exists(booksFile)) {
            seedDefaultData();
        }
    }

    /** 首次运行时写入默认账户与示例图书，便于演示。 */
    private void seedDefaultData() {
        users.add(new User(nextUserId++, "admin", "admin123", User.Role.ADMIN, ""));
        users.add(new User(nextUserId++, "user", "user123", User.Role.BORROWER, "计算机科学与技术"));
        books.add(new Book(nextBookId++, "Java编程思想", "Bruce Eckel", "自然科学类", 5));
        books.add(new Book(nextBookId++, "诗经", "佚名", "文学类", 3));
        books.add(new Book(nextBookId++, "中华上下五千年", "林汉达", "历史类", 4));
        books.add(new Book(nextBookId++, "理想国", "柏拉图", "哲学类", 2));
        books.add(new Book(nextBookId++, "金刚经", "鸠摩罗什", "宗教类", 3));
        books.add(new Book(nextBookId++, "艺术的故事", "贡布里希", "艺术类", 3));
        saveAll();
    }

    public void saveAll() {
        JsonUtil.save(usersFile, users);
        JsonUtil.save(booksFile, books);
        JsonUtil.save(loansFile, loans);
    }

    // ==================== 用户 ====================

    public User register(String username, String password, User.Role role, String major) {
        if (users.stream().anyMatch(u -> u.getUsername().equals(username))) {
            return null;
        }
        User user = new User(nextUserId++, username, password, role, major);
        users.add(user);
        saveAll();
        return user;
    }

    public User login(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst().orElse(null);
    }

    public User findUser(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>(users);
        }
        String k = keyword.toLowerCase();
        return users.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(k)
                        || String.valueOf(u.getId()).contains(k)
                        || (u.getMajor() != null && u.getMajor().toLowerCase().contains(k)))
                .collect(Collectors.toList());
    }

    // ==================== 图书 ====================

    public Book addBook(String title, String author, String type, int total) {
        Book book = new Book(nextBookId++, title, author, type, total);
        books.add(book);
        saveAll();
        return book;
    }

    public Book findBook(int id) {
        return books.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public List<Book> getAvailableBooks() {
        return books.stream().filter(b -> b.getAvailable() > 0).collect(Collectors.toList());
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>(books);
        }
        String k = keyword.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(k)
                        || b.getAuthor().toLowerCase().contains(k)
                        || b.getType().toLowerCase().contains(k)
                        || String.valueOf(b.getId()).contains(k))
                .collect(Collectors.toList());
    }

    // ==================== 借阅 ====================

    public Loan borrowBook(int userId, int bookId) {
        Book book = findBook(bookId);
        if (book == null || book.getAvailable() <= 0) {
            return null;
        }
        LocalDate today = LocalDate.now();
        Loan loan = new Loan(nextLoanId++, bookId, userId, today, today.plusDays(Loan.LOAN_DAYS), null);
        loans.add(loan);
        book.setAvailable(book.getAvailable() - 1);
        saveAll();
        return loan;
    }

    public boolean returnBook(int userId, int bookId) {
        Loan target = loans.stream()
                .filter(l -> l.getUserId() == userId && l.getBookId() == bookId && !l.isReturned())
                .min(Comparator.comparing(Loan::getBorrowDate))
                .orElse(null);
        if (target == null) {
            return false;
        }
        target.setReturnDate(LocalDate.now());
        Book book = findBook(bookId);
        if (book != null) {
            book.setAvailable(book.getAvailable() + 1);
        }
        saveAll();
        return true;
    }

    public List<Loan> getLoansByUser(int userId) {
        return loans.stream().filter(l -> l.getUserId() == userId).collect(Collectors.toList());
    }

    public List<Loan> getUnreturnedLoansByUser(int userId) {
        return loans.stream()
                .filter(l -> l.getUserId() == userId && !l.isReturned())
                .collect(Collectors.toList());
    }

    public List<Loan> searchLoans(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>(loans);
        }
        String k = keyword.toLowerCase();
        return loans.stream().filter(l -> {
            Book b = findBook(l.getBookId());
            User u = findUser(l.getUserId());
            return String.valueOf(l.getId()).contains(k)
                    || (b != null && (b.getTitle().toLowerCase().contains(k)
                            || b.getAuthor().toLowerCase().contains(k)
                            || String.valueOf(b.getId()).contains(k)))
                    || (u != null && (u.getUsername().toLowerCase().contains(k)
                            || String.valueOf(u.getId()).contains(k)));
        }).collect(Collectors.toList());
    }

    public List<Loan> getOverdueLoans() {
        LocalDate today = LocalDate.now();
        return loans.stream()
                .filter(l -> !l.isReturned() && l.getDueDate().isBefore(today))
                .collect(Collectors.toList());
    }

    public List<Loan> searchLoansByDateRange(LocalDate start, LocalDate end) {
        return loans.stream()
                .filter(l -> !l.getBorrowDate().isBefore(start) && !l.getBorrowDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<Loan> searchLoansByDateRange(int userId, LocalDate start, LocalDate end) {
        return loans.stream()
                .filter(l -> l.getUserId() == userId
                        && !l.getBorrowDate().isBefore(start) && !l.getBorrowDate().isAfter(end))
                .collect(Collectors.toList());
    }

    // ==================== 统计 ====================

    public Map<String, Integer> statisticsByBookType() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String type : Book.BOOK_TYPES) {
            counts.put(type, 0);
        }
        for (Loan l : loans) {
            Book b = findBook(l.getBookId());
            if (b != null) {
                counts.merge(b.getType(), 1, Integer::sum);
            }
        }
        return sortByValueDesc(counts);
    }

    public Map<String, Integer> statisticsByMajor() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Loan l : loans) {
            User u = findUser(l.getUserId());
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

    public String describeLoan(Loan l) {
        Book b = findBook(l.getBookId());
        User u = findUser(l.getUserId());
        String book = b == null ? "未知图书(ID:" + l.getBookId() + ")" : b.getTitle() + "(ID:" + b.getId() + ")";
        String user = u == null ? "未知用户(ID:" + l.getUserId() + ")" : u.getUsername() + "(ID:" + u.getId() + ")";
        String status;
        if (l.isReturned()) {
            status = "已归还(" + l.getReturnDate() + ")";
        } else if (l.isOverdue()) {
            status = "超期未还";
        } else {
            status = "借阅中";
        }
        return String.format("借阅ID:%d | 图书:%s | 用户:%s | 借阅:%s | 应还:%s | 状态:%s",
                l.getId(), book, user, l.getBorrowDate(), l.getDueDate(), status);
    }
}