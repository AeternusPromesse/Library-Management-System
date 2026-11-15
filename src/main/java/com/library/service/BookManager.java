package com.library.service;

import com.google.gson.reflect.TypeToken;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.util.JsonUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图书信息数据管理类：图书的新增、查询、废弃、编辑、副本编辑，并持久化到 JSON 文件。
 */
public class BookManager {

    private final Path booksFile;

    private List<Book> books = new ArrayList<>();
    private int nextBookId = 1;
    private int nextCopyId = 1;

    public BookManager() {
        booksFile = Paths.get(System.getProperty("user.dir"), "data", "books.json");
        books = JsonUtil.loadList(booksFile, new TypeToken<List<Book>>() {}.getType());
        if (books.isEmpty()) {
            books = init10Books();
            save();
        }
        nextBookId = books.stream().mapToInt(Book::getId).max().orElse(0) + 1;
        int maxCopyId = 0;
        for (Book b : books) {
            if (b.getBookCopies() == null) {
                continue;
            }
            for (BookCopy c : b.getBookCopies()) {
                maxCopyId = Math.max(maxCopyId, c.getCopyId());
            }
        }
        nextCopyId = maxCopyId + 1;
    }

    /**
     * 添加图书：根据 stock 值自动为新书创建 stock 个副本；新书插入列表首位，显示在图书列表第一行。
     */
    public Book addBook(String title, String author, String type, String publisher,
                        int days, double price, int stock) {
        Book book = new Book(nextBookId++, title, author, type, publisher, days, price, stock);
        for (int i = 0; i < stock; i++) {
            book.addCopy(new BookCopy(nextCopyId++, book.getId()));
        }
        books.add(0, book); // 新图书显示在列表第一行
        save();
        return book;
    }

    public Book findBook(int id) {
        return books.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }

    /**
     * 查询书籍：关键字模糊匹配书名/作者/类型/出版社；关键字为空返回全部图书（新书在前）。
     */
    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>(books);
        }
        String k = keyword.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(k)
                        || b.getAuthor().toLowerCase().contains(k)
                        || b.getType().toLowerCase().contains(k)
                        || (b.getPublisher() != null && b.getPublisher().toLowerCase().contains(k))
                        || String.valueOf(b.getId()).contains(k))
                .collect(Collectors.toList());
    }

    /** 正常状态的图书列表 */
    public List<Book> getNormalBooks() {
        return books.stream()
                .filter(b -> Book.STATUS_NORMAL.equals(b.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 废弃图书：全部副本归还图书馆后（可借副本数与库存一致）才允许废弃，
     * 废弃后图书状态改为"已废弃"，不删除借阅记录。
     */
    public String discardBook(int bookId) {
        Book book = findBook(bookId);
        if (book == null) {
            return "废弃失败：图书不存在";
        }
        if (Book.STATUS_DISCARDED.equals(book.getStatus())) {
            return "废弃失败：该图书已是废弃状态";
        }
        if (book.countAvailable() != book.getStock()) {
            return "废弃失败：还有副本外借，不能废弃";
        }
        book.setStatus(Book.STATUS_DISCARDED);
        book.update();
        save();
        return "废弃成功：《" + book.getTitle() + "》已标记为已废弃，借阅记录保留";
    }

    /**
     * 编辑图书：只更新 title、author、type、publisher、days、price；
     * 不更新 stock（库存）、bookCopies（副本列表）、status（状态）。
     */
    public Book updateBook(int id, String title, String author, String type,
                           String publisher, int days, double price) {
        Book book = findBook(id);
        if (book == null) {
            return null;
        }
        book.setTitle(title);
        book.setAuthor(author);
        book.setType(type);
        book.setPublisher(publisher);
        book.setDays(days);
        book.setPrice(price);
        book.update();
        save();
        return book;
    }

    /** 新增副本：副本状态默认"正常"，新增后图书库存 stock 加 1 */
    public BookCopy addCopy(int bookId) {
        Book book = findBook(bookId);
        if (book == null || !Book.STATUS_NORMAL.equals(book.getStatus())) {
            return null;
        }
        BookCopy copy = new BookCopy(nextCopyId++, bookId);
        book.addCopy(copy);
        book.refreshStock();
        book.update();
        save();
        return copy;
    }

    /**
     * 修改副本状态（正常/损坏/废弃）：外借中的副本不能修改状态；
     * 修改后重新统计正常副本数，保持库存 stock 与状态正常的副本数一致。
     */
    public String changeCopyStatus(int bookId, int copyId, String newStatus) {
        Book book = findBook(bookId);
        if (book == null) {
            return "修改失败：图书不存在";
        }
        BookCopy copy = book.getBookCopies() == null ? null
                : book.getBookCopies().stream()
                .filter(c -> c.getCopyId() == copyId)
                .findFirst().orElse(null);
        if (copy == null) {
            return "修改失败：副本不存在";
        }
        if (copy.isBorrowed() && !BookCopy.STATUS_NORMAL.equals(newStatus)) {
            return "修改失败：副本外借中，不能修改状态";
        }
        copy.setStatus(newStatus);
        copy.update();
        book.refreshStock();
        book.update();
        save();
        return "修改成功：副本#" + copyId + " 状态已改为「" + newStatus + "」，当前库存:" + book.getStock();
    }

    public void save() {
        JsonUtil.save(booksFile, books);
    }

    /**
     * 初始化 10 本图书，每本书 5 个副本，覆盖 7 种图书类型。
     */
    private static List<Book> init10Books() {
        List<Book> list = new ArrayList<>();
        int copyId = 1;
        Object[][] data = {
                {"Java编程思想", "Bruce Eckel", "自然科学类", "机械工业出版社", 30, 108.0},
                {"诗经", "佚名", "文学类", "中华书局", 15, 32.0},
                {"中华上下五千年", "林汉达", "历史类", "少年儿童出版社", 30, 58.0},
                {"理想国", "柏拉图", "哲学类", "商务印书馆", 30, 45.0},
                {"金刚经", "鸠摩罗什（译）", "宗教类", "中华书局", 20, 25.0},
                {"艺术的故事", "贡布里希", "艺术类", "广西美术出版社", 30, 228.0},
                {"时间简史", "史蒂芬·霍金", "自然科学类", "湖南科学技术出版社", 30, 45.0},
                {"红楼梦", "曹雪芹", "文学类", "人民文学出版社", 30, 59.7},
                {"论语译注", "杨伯峻", "哲学类", "中华书局", 20, 28.0},
                {"乡土中国", "费孝通", "社会科学类", "人民出版社", 20, 25.0},
        };
        for (int i = 0; i < data.length; i++) {
            Book book = new Book(i + 1, (String) data[i][0], (String) data[i][1],
                    (String) data[i][2], (String) data[i][3],
                    (Integer) data[i][4], (Double) data[i][5], 5);
            for (int j = 0; j < 5; j++) {
                book.addCopy(new BookCopy(copyId++, book.getId()));
            }
            list.add(book);
        }
        return list;
    }
}
