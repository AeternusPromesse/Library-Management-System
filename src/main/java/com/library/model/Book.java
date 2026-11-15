package com.library.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 图书实体类。
 */
public class Book extends Base {

    /** 系统支持的 7 种图书类型（统计时需全部展示） */
    public static final String[] BOOK_TYPES = {
            "哲学类", "宗教类", "社会科学类", "自然科学类",
            "文学类", "历史类", "艺术类"
    };

    /** 图书状态 */
    public static final String STATUS_NORMAL = "正常";
    public static final String STATUS_DISCARDED = "已废弃";

    private String title;     // 书名
    private String author;    // 作者
    private String type;      // 图书类型
    private String publisher; // 出版社
    private int days;         // 还书期限（天）
    private double price;     // 价格
    private int stock;        // 库存（状态正常的副本数量）
    private String status = STATUS_NORMAL; // 状态：正常 / 已废弃
    private List<BookCopy> bookCopies = new ArrayList<>(); // 副本列表

    public Book() {
    }

    public Book(int id, String title, String author, String type, String publisher,
                int days, double price, int stock) {
        setId(id);
        this.title = title;
        this.author = author;
        this.type = type;
        this.publisher = publisher;
        this.days = days;
        this.price = price;
        this.stock = stock;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BookCopy> getBookCopies() {
        return bookCopies;
    }

    public void setBookCopies(List<BookCopy> bookCopies) {
        this.bookCopies = bookCopies;
    }

    /** 新增一个物理副本 */
    public void addCopy(BookCopy copy) {
        if (this.bookCopies == null) {
            this.bookCopies = new ArrayList<>();
        }
        this.bookCopies.add(copy);
    }

    /** 获取一个可借副本（状态正常且未借出），无则返回 null */
    public BookCopy getAvailableCopy() {
        if (bookCopies == null) {
            return null;
        }
        return bookCopies.stream()
                .filter(BookCopy::isAvailable)
                .findFirst().orElse(null);
    }

    /** 统计可借副本数（状态正常且未借出） */
    public long countAvailable() {
        if (bookCopies == null) {
            return 0;
        }
        return bookCopies.stream().filter(BookCopy::isAvailable).count();
    }

    /** 按正常副本数刷新库存，保持 stock 与状态正常的副本数一致 */
    public void refreshStock() {
        if (bookCopies == null) {
            this.stock = 0;
            return;
        }
        this.stock = (int) bookCopies.stream()
                .filter(c -> BookCopy.STATUS_NORMAL.equals(c.getStatus()))
                .count();
    }

    @Override
    public String toString() {
        return String.format("ID:%d | 书名:%s | 作者:%s | 类型:%s | 出版社:%s | 期限:%d天 | 价格:%.1f元 | 库存:%d | 可借:%d | 状态:%s",
                getId(), title, author, type, publisher, days, price, stock, countAvailable(), status);
    }
}
