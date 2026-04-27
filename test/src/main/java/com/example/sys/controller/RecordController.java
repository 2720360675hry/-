package com.example.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.sys.common.Result;
import com.example.sys.entity.Book;
import com.example.sys.entity.User;
import com.example.sys.entity.Record;
import com.example.sys.service.IBookService;
import com.example.sys.service.IRecordService;
import com.example.sys.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xumj
 * @since 2025-09-08
 */
@RestController
@RequestMapping("/sys/record")
public class RecordController {
    @Autowired
    private IRecordService IRecordService;

    //历史借阅记录
    @GetMapping("/pastBorrowedRecords")
    public Result pastBorrowedRecords(){
        User user = ThreadLocalUtil.get();
        LambdaQueryWrapper<Record> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(Record::getRecordBorrower,user.getUserName());
        List<Record> recordList = IRecordService.list(recordWrapper);
        System.out.println("曾经的借阅记录");
        return Result.success(recordList);
    }

}
