package com.library.model;

/**
 * 图书推荐实体类：教师推荐图书，管理员审阅。
 */
public class Recommendation extends Base {

    /** 推荐状态 */
    public static final String STATUS_PENDING = "待审阅";
    public static final String STATUS_APPROVED = "已通过";
    public static final String STATUS_REJECTED = "已驳回";

    private String title;   // 推荐书名
    private String author; // 作者
    private int userId;    // 推荐人用户ID
    private String status = STATUS_PENDING; // 状态：待审阅 / 已通过 / 已驳回

    public Recommendation() {
    }

    public Recommendation(int id, String title, String author, int userId) {
        setId(id);
        this.title = title;
        this.author = author;
        this.userId = userId;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("推荐ID:%d | 《%s》 %s | 推荐人ID:%d | 状态:%s",
                getId(), title, author, userId, status);
    }
}
