package com.graduation.agriplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.graduation.agriplatform.entity.User;
import com.graduation.agriplatform.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    // 1. 登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            result.put("code", 400); result.put("msg", "账号不存在！"); return result;
        }
        if (!user.getPassword().equals(password)) {
            result.put("code", 400); result.put("msg", "密码错误！"); return result;
        }
        if (user.getStatus() == 0) {
            result.put("code", 403); result.put("msg", "账号已被冻结，请联系管理员！"); return result;
        }

        result.put("code", 200);
        result.put("msg", "登录成功");
        result.put("data", user);
        return result;
    }

    // 2. 注册接口 (新增)
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            result.put("code", 400);
            result.put("msg", "该账号已被注册，请更换账号名！");
            return result;
        }

        // 设置默认属性并保存
        user.setRoleCode("user"); // 新注册的一律为普通用户
        user.setStatus(1);        // 状态默认正常
        userMapper.insert(user);

        result.put("code", 200);
        result.put("msg", "注册成功，请登录");
        return result;
    }
}