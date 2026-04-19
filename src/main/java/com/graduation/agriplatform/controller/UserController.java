package com.graduation.agriplatform.controller;

import com.graduation.agriplatform.entity.User;
import com.graduation.agriplatform.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 获取所有用户列表 (管理员用)
    @GetMapping("/list")
    public List<User> list() {
        return userMapper.selectList(null);
    }

    // 冻结/解冻账号
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestBody User user) {
        userMapper.updateById(user);
        return "状态更新成功";
    }
}