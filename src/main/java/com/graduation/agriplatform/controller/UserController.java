package com.graduation.agriplatform.controller;

import com.graduation.agriplatform.entity.User;
import com.graduation.agriplatform.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 1. 查询用户列表
    @GetMapping("/list")
    public List<User> list() {
        return userMapper.selectList(null);
    }

    // 2. 新增用户
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        userMapper.insert(user);
        result.put("code", 200);
        result.put("msg", "新增成功");
        return result;
    }

    // 3. 修改用户资料 (包括状态冻结/解冻)
    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        userMapper.updateById(user);
        result.put("code", 200);
        result.put("msg", "更新成功");
        return result;
    }

    // 4. 删除用户
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        userMapper.deleteById(id);
        result.put("code", 200);
        result.put("msg", "删除成功");
        return result;
    }
}