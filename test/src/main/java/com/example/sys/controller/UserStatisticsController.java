package com.example.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.sys.common.Result;
import com.example.sys.entity.UserStatistics;
import com.example.sys.service.IUserStatisticsService;
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
@RequestMapping("/sys/userStatistics")
public class UserStatisticsController {
    @Autowired
    private IUserStatisticsService userStatisticsService;

    @RequestMapping("/getUserStatistics")
    public Result getUserStatistics(){
        // 查询 user_statistics 表数据 activity_count 降序排列 返回前20名活跃用户
        QueryWrapper<UserStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("activity_count");
        List<UserStatistics> list = userStatisticsService.list(queryWrapper);
        return Result.success(list.subList(0,20));
    }
}
