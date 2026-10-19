package com.library.model;

/**
 * 图书副本实体类：表示某本图书的一个物理副本。
 */
public class BookCopy {

    private int copyId;      // 副本唯一ID
    private int bookId;      // 所属图书ID
    private boolean borrowed; // 是否已借出

    public BookCopy() {
    }

    public BookCopy(int copyId, int bookId) {
        this.copyId = copyId;
        this.bookId = bookId;
        this.borrowed = false;
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

    /** 副本是否可借（未被借出） */
    public boolean isAvailable() {
        return !borrowed;
    }

    /** 借出该副本：成功返回 true，已被借出返回 false */
    public boolean borrow() {
        if (borrowed) {
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
        return String.format("副本ID:%d | 图书ID:%d | 状态:%s",
                copyId, bookId, borrowed ? "已借出" : "在馆");
    }
}