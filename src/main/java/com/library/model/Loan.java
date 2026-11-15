package com.library.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 借阅记录实体类。
 */
public class Loan extends Base {

    /** 逾期罚款金额（元/天） */
    public static final double FINE_PER_DAY = 0.5;

    private int bookId;           // 图书ID
    private int userId;           // 用户ID
    private int copyId;           // 所借副本ID
    private LocalDate borrowDate; // 借阅日期
    private LocalDate dueDate;    // 应还日期
    private LocalDate returnDate; // 实际归还日期（null 表示未还）
    private double fine;          // 逾期罚款金额

    public Loan() {
    }

    public Loan(int id, int bookId, int userId, int copyId,
                LocalDate borrowDate, LocalDate dueDate) {
        setId(id);
        this.bookId = bookId;
        this.userId = userId;
        this.copyId = copyId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCopyId() {
        return copyId;
    }

    public void setCopyId(int copyId) {
        this.copyId = copyId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    /** 是否已归还 */
    public boolean isReturned() {
        return returnDate != null;
    }

    /** 是否超期（未归还且已过应还日期） */
    public boolean isOverdue() {
        return returnDate == null && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    /** 执行归还操作：设置实际归还日期并计算逾期罚款 */
    public boolean returnBook() {
        if (returnDate != null) {
            return false;
        }
        setReturnDate(LocalDate.now());
        setFine(calcOverdueFee(dueDate, returnDate));
        update();
        return true;
    }

    /** 静态逾期计费：逾期天数 × 每日罚款，未逾期或未归还返回 0 */
    public static double calcOverdueFee(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null || !returnDate.isAfter(dueDate)) {
            return 0.0;
        }
        return ChronoUnit.DAYS.between(dueDate, returnDate) * FINE_PER_DAY;
    }
}
