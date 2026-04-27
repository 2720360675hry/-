package com.example.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author xumj
 * @since 2025-09-08
 */
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图书编号
     */
    @TableId(value = "book_id", type = IdType.AUTO)
    private Integer bookId;

    /**
     * 图书名称
     */
    private String bookName;

    /**
     * 图书标准ISBN编号
     */
    private String bookIsbn;

    /**
     * 图书出版社
     */
    private String bookPress;

    /**
     * 图书作者
     */
    private String bookAuthor;

    /**
     * 图书页数
     */
    private Integer bookPagination;

    /**
     * 图书价格
     */
    private Double bookPrice;

    /**
     * 图书上架时间
     */
    private String bookUploadtime;

    /**
     * 图书状态（0：可借阅，1:已借阅，2：归还中，3：已下架）
     */
    private String bookStatus;

    /**
     * 图书借阅人
     */
    private String bookBorrower;

    /**
     * 图书借阅时间
     */
    private String bookBorrowtime;

    /**
     * 图书预计归还时间
     */
    private String bookReturntime;

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }
    public String getBookPress() {
        return bookPress;
    }

    public void setBookPress(String bookPress) {
        this.bookPress = bookPress;
    }
    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
    public Integer getBookPagination() {
        return bookPagination;
    }

    public void setBookPagination(Integer bookPagination) {
        this.bookPagination = bookPagination;
    }
    public Double getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(Double bookPrice) {
        this.bookPrice = bookPrice;
    }
    public String getBookUploadtime() {
        return bookUploadtime;
    }

    public void setBookUploadtime(String bookUploadtime) {
        this.bookUploadtime = bookUploadtime;
    }
    public String getBookStatus() {
        return bookStatus;
    }

    public void setBookStatus(String bookStatus) {
        this.bookStatus = bookStatus;
    }
    public String getBookBorrower() {
        return bookBorrower;
    }

    public void setBookBorrower(String bookBorrower) {
        this.bookBorrower = bookBorrower;
    }
    public String getBookBorrowtime() {
        return bookBorrowtime;
    }

    public void setBookBorrowtime(String bookBorrowtime) {
        this.bookBorrowtime = bookBorrowtime;
    }
    public String getBookReturntime() {
        return bookReturntime;
    }

    public void setBookReturntime(String bookReturntime) {
        this.bookReturntime = bookReturntime;
    }

    @Override
    public String toString() {
        return "Book{" +
            "bookId=" + bookId +
            ", bookName=" + bookName +
            ", bookIsbn=" + bookIsbn +
            ", bookPress=" + bookPress +
            ", bookAuthor=" + bookAuthor +
            ", bookPagination=" + bookPagination +
            ", bookPrice=" + bookPrice +
            ", bookUploadtime=" + bookUploadtime +
            ", bookStatus=" + bookStatus +
            ", bookBorrower=" + bookBorrower +
            ", bookBorrowtime=" + bookBorrowtime +
            ", bookReturntime=" + bookReturntime +
        "}";
    }
}
