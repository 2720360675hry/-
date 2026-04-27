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
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 借阅记录id
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Integer recordId;

    /**
     * 借阅的图书名称
     */
    private String recordBookname;

    /**
     * 借阅的图书的ISBN编号
     */
    private String recordBookisbn;

    /**
     * 图书借阅人
     */
    private String recordBorrower;

    /**
     * 图书借阅时间
     */
    private String recordBorrowtime;

    /**
     * 图书归还时间
     */
    private String recordRemandtime;

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }
    public String getRecordBookname() {
        return recordBookname;
    }

    public void setRecordBookname(String recordBookname) {
        this.recordBookname = recordBookname;
    }
    public String getRecordBookisbn() {
        return recordBookisbn;
    }

    public void setRecordBookisbn(String recordBookisbn) {
        this.recordBookisbn = recordBookisbn;
    }
    public String getRecordBorrower() {
        return recordBorrower;
    }

    public void setRecordBorrower(String recordBorrower) {
        this.recordBorrower = recordBorrower;
    }
    public String getRecordBorrowtime() {
        return recordBorrowtime;
    }

    public void setRecordBorrowtime(String recordBorrowtime) {
        this.recordBorrowtime = recordBorrowtime;
    }
    public String getRecordRemandtime() {
        return recordRemandtime;
    }

    public void setRecordRemandtime(String recordRemandtime) {
        this.recordRemandtime = recordRemandtime;
    }

    @Override
    public String toString() {
        return "Record{" +
            "recordId=" + recordId +
            ", recordBookname=" + recordBookname +
            ", recordBookisbn=" + recordBookisbn +
            ", recordBorrower=" + recordBorrower +
            ", recordBorrowtime=" + recordBorrowtime +
            ", recordRemandtime=" + recordRemandtime +
        "}";
    }
}
