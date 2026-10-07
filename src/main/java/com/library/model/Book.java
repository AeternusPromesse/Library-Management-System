package com.library.model;

/**
 * 图书实体类。
 */
public class Book {

    /** 系统支持的 7 种图书类型（统计时需全部展示） */
    public static final String[] BOOK_TYPES = {
            "哲学类", "宗教类", "社会科学类", "自然科学类",
            "文学类", "历史类", "艺术类"
    };

    private int id;          // 图书ID
    private String title;    // 书名
    private String author;   // 作者
    private String type;     // 图书类型
    private int total;       // 总库存
    private int available;   // 当前可借数量

    public Book() {
    }

    public Book(int id, String title, String author, String type, int total) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.type = type;
        this.total = total;
        this.available = total;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("ID:%d | 书名:%s | 作者:%s | 类型:%s | 可借:%d/总:%d",
                id, title, author, type, available, total);
    }
}