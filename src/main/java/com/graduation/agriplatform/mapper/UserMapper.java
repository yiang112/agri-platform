package com.graduation.agriplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graduation.agriplatform.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}