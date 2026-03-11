package com.graduation.agriplatform.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

@TableName("agriculture_data")
public class AgricultureData {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ✅ 新字段：省份
    private String province;

    // ✅ 新字段：年份
    private Integer year;

    private Double temperature;
    private Double rainfall;
    private Double soilPh;
    private Double soilHumidity;
    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;
    private String cropType;
    private Double yieldAmount;
    private Date createTime;

    // --- 下面是 Getter 和 Setter 方法 ---
    // (请确保没有 getRegion 和 setRegion)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getRainfall() { return rainfall; }
    public void setRainfall(Double rainfall) { this.rainfall = rainfall; }

    public Double getSoilPh() { return soilPh; }
    public void setSoilPh(Double soilPh) { this.soilPh = soilPh; }

    public Double getSoilHumidity() { return soilHumidity; }
    public void setSoilHumidity(Double soilHumidity) { this.soilHumidity = soilHumidity; }

    public Double getYieldAmount() { return yieldAmount; }
    public void setYieldAmount(Double yieldAmount) { this.yieldAmount = yieldAmount; }

    // ... 其他 getter/setter ...
}