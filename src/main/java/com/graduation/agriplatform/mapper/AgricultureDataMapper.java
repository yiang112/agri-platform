package com.graduation.agriplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graduation.agriplatform.entity.AgricultureData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据访问层，直接继承 BaseMapper 即可拥有 CRUD 能力
 */
@Mapper
public interface AgricultureDataMapper extends BaseMapper<AgricultureData> {
    // 这里暂时不需要写SQL，MyBatis-Plus 会自动帮我们生成
}