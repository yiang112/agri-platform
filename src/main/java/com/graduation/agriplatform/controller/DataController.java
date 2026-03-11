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
@CrossOrigin
public class DataController {

    @Autowired
    private AgricultureDataMapper dataMapper;

    // 1. 获取所有数据
    @GetMapping("/list")
    public List<AgricultureData> getAllData() {
        return dataMapper.selectList(null);
    }

    // 2. 添加数据
    @PostMapping("/add")
    public String addData(@RequestBody AgricultureData data) {
        int result = dataMapper.insert(data);
        return result > 0 ? "添加成功" : "添加失败";
    }

    // 3. (新功能) 调用 Python 进行预测
    // 浏览器访问：http://localhost:8080/api/data/predict
    // 修改 predict 接口，让它接收 province 参数
    @GetMapping("/predict")
    public String predictYield(@RequestParam(value = "province", defaultValue = "黑龙江") String province) {
        try {
            // 1. 设置 Python 脚本路径 (请确认这个路径是对的)
            String pythonExePath = "D:\\homework\\python\\.venv\\Scripts\\python.exe"; // 你的 Python 路径
            String scriptPath = "E:\\thesis\\agri-platform\\predict_script.py"; // 你的脚本路径

            // 2. 准备传给 Python 的参数
            // 顺序必须是: 脚本路径, 省份, 年份, 温度, 降水, pH, 湿度
            // 这里我们先模拟 2025 年的气候 (未来你可以从数据库查该省的最新气象数据传进去)
            String year = "2025";
            String temp = "22.5";
            String rain = "600";
            String ph = "6.5";
            String humidity = "55";

            // 3. 构建命令行
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExePath,
                    scriptPath,
                    province, // ✅ 把前端传来的省份传给 Python
                    year, temp, rain, ph, humidity
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 4. 读取 Python 的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "预测出错 (Exit Code " + exitCode + "): " + output.toString();
            }

            return output.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "发生错误: " + e.getMessage();
        }
    }
    // 4. 更新数据 (用于网页上修改错误的数据)
// === 新增接口：修改数据 ===
    // 前端会把修改后的整行数据以 JSON 格式发过来
    @PostMapping("/update")
    public String updateData(@RequestBody AgricultureData data) {
        // updateById 是 MyBatis-Plus 自带的神技
        // 它会自动根据 id 找到那一行，把其他字段更新掉
        int result = dataMapper.updateById(data);

        if (result > 0) {
            return "修改成功";
        } else {
            return "修改失败";
        }
    }
    // 5. 删除数据 (用于网页上删除不要的数据)
    @PostMapping("/delete")
    public String deleteData(@RequestParam Integer id) {
        // Mybatis-Plus 提供的根据 ID 删除的方法
        int i = dataMapper.deleteById(id);
        return i > 0 ? "删除成功" : "删除失败";
    }
    // === 新增接口：获取某省的历史产量趋势 ===
    @GetMapping("/history")
    public List<AgricultureData> getHistoryData(@RequestParam String province) {
        // 使用 MyBatis-Plus 的查询构造器
        QueryWrapper<AgricultureData> queryWrapper = new QueryWrapper<>();

        // 逻辑：查找 province = "河南"，并按 year (年份) 升序排列
        queryWrapper.eq("province", province);
        queryWrapper.orderByAsc("year");

        return dataMapper.selectList(queryWrapper);
    }
    // === 新增接口：获取数据库里所有的省份名称 ===
    @GetMapping("/provinces")
    public List<Object> getAllProvinces() {
        // SELECT DISTINCT province FROM agriculture_data
        QueryWrapper<AgricultureData> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT province"); // 去重查询
        return dataMapper.selectObjs(queryWrapper); // 只返回第一列(即省份名)
    }

}