package com.graduation.agriplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.graduation.agriplatform.entity.AgricultureData;
import com.graduation.agriplatform.mapper.AgricultureDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*") // 允许前端 Vue 跨域请求
public class DataController {

    @Autowired
    private AgricultureDataMapper dataMapper;

    /**
     * 1. 获取所有数据（用于数据管理界面的表格展示）
     */
    @GetMapping("/list")
    public List<AgricultureData> getAllData() {
        // selectList(null) 表示查询所有数据，没有条件
        return dataMapper.selectList(null);
    }

    /**
     * 2. 获取数据库中所有不重复的省份名称（用于前端下拉框动态加载）
     */
    @GetMapping("/provinces")
    public List<Object> getAllProvinces() {
        QueryWrapper<AgricultureData> queryWrapper = new QueryWrapper<>();
        // 使用 DISTINCT 去重，只查省份这一列
        queryWrapper.select("DISTINCT province");
        return dataMapper.selectObjs(queryWrapper);
    }

    /**
     * 3. 获取特定【省份】和【农作物】的历史数据（用于 ECharts 历史折线图）
     */
    @GetMapping("/history")
    public List<AgricultureData> getHistoryData(
            @RequestParam String province,
            @RequestParam String cropType) {

        QueryWrapper<AgricultureData> queryWrapper = new QueryWrapper<>();
        // 同时根据省份和农产品种类筛选
        queryWrapper.eq("province", province);
        queryWrapper.eq("crop_type", cropType);
        // 按照年份升序排列，保证折线图从左到右时间递增
        queryWrapper.orderByAsc("year");

        return dataMapper.selectList(queryWrapper);
    }

    /**
     * 4. 调用 Python 机器学习脚本进行智能预测
     */
    @GetMapping("/predict")
    public String predictYield(
            @RequestParam String province,
            @RequestParam String cropType) {

        StringBuilder result = new StringBuilder();
        try {
            // 拼接 Python 执行命令，依次传入：脚本名 省份 农作物品种
            // 示例：python predict_script.py 黑龙江 大豆
            String command = "python predict_script.py " + province + " " + cropType;
            Process process = Runtime.getRuntime().exec(command);

            // 读取 Python 脚本的标准输出流 (控制台打印的内容)
            // 注意：Windows 下 Python 默认输出可能为 GBK 编码，如果控制台乱码请将 GBK 改为 UTF-8
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            // 读取 Python 脚本的错误输出流 (方便在 Java 控制台排查 Python 报错)
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "GBK"));
            while ((line = errorReader.readLine()) != null) {
                System.err.println("Python Error: " + line);
            }

            process.waitFor(); // 等待 Python 脚本执行结束
        } catch (Exception e) {
            e.printStackTrace();
            return "预测调用失败：" + e.getMessage();
        }
        return result.toString();
    }

    /**
     * 5. 删除单条数据
     */
    @DeleteMapping("/delete")
    public String deleteData(@RequestParam Long id) {
        int result = dataMapper.deleteById(id);
        return result > 0 ? "删除成功" : "删除失败";
    }

    /**
     * 6. 修改/更新数据（通过传递包含 ID 的完整对象来更新）
     */
    @PostMapping("/update")
    public String updateData(@RequestBody AgricultureData data) {
        // updateById 会自动根据传入对象的 id 去更新其他有值的字段
        int result = dataMapper.updateById(data);
        return result > 0 ? "修改成功" : "修改失败";
    }
}