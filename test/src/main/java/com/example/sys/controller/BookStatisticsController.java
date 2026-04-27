package com.example.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.sys.common.Result;
import com.example.sys.entity.BookStatistics;
import com.example.sys.service.IBookStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xumj
 * @since 2025-10-13
 */
@RestController
@CrossOrigin
@RequestMapping("/sys/bookStatistics")
public class BookStatisticsController {
    @Autowired
    private IBookStatisticsService bookStatisticsService;

    /**
     * 1. 获取热门图书排行前20的数据
     */
    @RequestMapping("/top20")
    public Result top20() {
        // 新建条件构造器
        QueryWrapper<BookStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("borrow_count");
        // list(按照你的条件进行查询)
        List<BookStatistics> books = bookStatisticsService.list(queryWrapper);
        return Result.success(books.subList(0, 10));
    }
}
