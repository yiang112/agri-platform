package com.graduation.agriplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("agriculture_data")
public class AgricultureData {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String province;

    // 🌟 核心修复：加上农作物品种字段！没有它前端无法区分作物！
    private String cropType;

    private Integer year;
    private Double temperature;
    private Double rainfall;
    private Double soilPh;
    private Double soilHumidity;
    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;

    // 确保产量的命名是驼峰式
    private Double yieldAmount;
}