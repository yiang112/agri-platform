package com.graduation.agriplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@SpringBootApplication
@MapperScan("com.graduation.agriplatform.mapper")
public class AgriPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriPlatformApplication.class, args);
    }

    /**
     * === 核心配置区域 ===
     */

    // 1. 保留这个：手动配置数据库连接
    // (这能解决 application.properties 乱码读不到的问题)
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/agri_yield_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("123456"); // <--- 确认你的密码
        return dataSource;
    }

    // 2. 注意！！！我删除了 sqlSessionFactory 方法
    // 删除后，Spring Boot 会自动接管，MyBatis-Plus 的 selectList 方法就会生效了！
}