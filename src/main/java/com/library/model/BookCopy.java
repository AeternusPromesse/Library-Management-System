package com.library.model;

/**
 * 图书副本实体类：表示某本图书的一个物理副本。
 */
public class BookCopy extends Base {

    /** 副本状态 */
    public static final String STATUS_NORMAL = "正常";
    public static final String STATUS_DAMAGED = "损坏";
    public static final String STATUS_DISCARDED = "废弃";

    private int copyId;      // 副本唯一ID
    private int bookId;      // 所属图书ID
    private boolean borrowed; // 是否已借出
    private String status = STATUS_NORMAL; // 状态：正常 / 损坏 / 废弃

    public BookCopy() {
    }

    public BookCopy(int copyId, int bookId) {
        this.copyId = copyId;
        this.bookId = bookId;
        setId(copyId);
    }

    public int getCopyId() {
        return copyId;
    }

    public void setCopyId(int copyId) {
        this.copyId = copyId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public boolean isBorrowed() {
        return borrowed;
    }

    public void setBorrowed(boolean borrowed) {
        this.borrowed = borrowed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** 副本是否可借（状态正常且未被借出） */
    public boolean isAvailable() {
        return !borrowed && STATUS_NORMAL.equals(status);
    }

    /** 借出该副本：成功返回 true，不可借返回 false */
    public boolean borrow() {
        if (borrowed || !STATUS_NORMAL.equals(status)) {
            return false;
        }
        borrowed = true;
        return true;
    }

    /** 归还该副本：成功返回 true，本来就在馆返回 false */
    public boolean returnCopy() {
        if (!borrowed) {
            return false;
        }
        borrowed = false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("副本ID:%d | 图书ID:%d | 状态:%s | 是否外借:%s",
                copyId, bookId, status, borrowed ? "是" : "否");
    }
}
