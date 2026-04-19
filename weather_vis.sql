/* 创建数据库 */
CREATE DATABASE IF NOT EXISTS agri_yield_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

USE agri_yield_db;

/* 1. 农业基础数据表：存储气象、土壤和历史产量数据 */
/* 对应开题报告中的“数据采集与预处理模块” */
DROP TABLE IF EXISTS `agriculture_data`;
CREATE TABLE `agriculture_data` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `region` varchar(100) NOT NULL COMMENT '种植区域(如：A区地块)',
                                    `record_date` date NOT NULL COMMENT '记录日期',
                                    `temperature` decimal(5,2) DEFAULT NULL COMMENT '平均气温(℃)',
                                    `rainfall` decimal(8,2) DEFAULT NULL COMMENT '降雨量(mm)',
                                    `soil_ph` decimal(4,2) DEFAULT NULL COMMENT '土壤PH值',
                                    `soil_humidity` decimal(5,2) DEFAULT NULL COMMENT '土壤湿度(%)',
                                    `nitrogen` decimal(8,2) DEFAULT NULL COMMENT '氮肥含量(mg/kg)',
                                    `phosphorus` decimal(8,2) DEFAULT NULL COMMENT '磷肥含量(mg/kg)',
                                    `potassium` decimal(8,2) DEFAULT NULL COMMENT '钾肥含量(mg/kg)',
                                    `crop_type` varchar(50) DEFAULT NULL COMMENT '作物种类(如：小麦、玉米)',
                                    `yield_amount` decimal(10,2) DEFAULT NULL COMMENT '实际产量(kg/亩，用于训练或记录)',
                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '数据录入时间',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农业基础数据表';

/* 2. 预测记录表：存储Python模型预测出的结果 */
/* 对应开题报告中的“产量预测引擎”输出 */
DROP TABLE IF EXISTS `prediction_record`;
CREATE TABLE `prediction_record` (
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                     `input_data_json` text COMMENT '预测时的输入参数快照(JSON格式)',
                                     `predicted_yield` decimal(10,2) NOT NULL COMMENT '模型预测产量',
                                     `algorithm_model` varchar(50) DEFAULT 'RandomForest' COMMENT '使用的算法模型',
                                     `prediction_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '预测时间',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产量预测记录表';

/* 3. 智能决策建议表：根据预测结果生成的建议 */
/* 对应开题报告中的“智能决策系统” */
DROP TABLE IF EXISTS `decision_advice`;
CREATE TABLE `decision_advice` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                   `prediction_id` bigint(20) NOT NULL COMMENT '关联的预测ID',
                                   `advice_type` varchar(50) COMMENT '建议类型(灌溉/施肥/病虫害)',
                                   `content` varchar(500) NOT NULL COMMENT '具体建议内容',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能决策建议表';

/* 插入几条测试数据，方便后面写代码测试 */
INSERT INTO `agriculture_data` (`region`, `record_date`, `temperature`, `rainfall`, `soil_ph`, `soil_humidity`, `crop_type`, `yield_amount`)
VALUES
    ('一号试验田', '2025-06-01', 25.5, 120.5, 6.8, 45.0, '小麦', 450.0),
    ('一号试验田', '2025-06-02', 26.0, 0.0, 6.7, 40.0, '小麦', 455.0),
    ('二号试验田', '2025-06-01', 24.0, 130.0, 7.1, 55.0, '玉米', 600.0);
-- 1. 清空旧的测试数据（为了防止结构冲突，建议先清空）
TRUNCATE TABLE agriculture_data;

-- 2. 添加 'province' (省份) 和 'year' (年份) 字段
ALTER TABLE agriculture_data
    ADD COLUMN province VARCHAR(50) NOT NULL COMMENT '省份' AFTER id,
    ADD COLUMN year INT NOT NULL COMMENT '年份' AFTER province;
-- 允许 region 字段为空
ALTER TABLE agriculture_data MODIFY COLUMN region VARCHAR(255) NULL;
-- 删除多余的 region 字段
ALTER TABLE agriculture_data DROP COLUMN region;
ALTER TABLE agriculture_data MODIFY COLUMN record_date DATETIME NULL;
-- 1. 给表添加一个自增的主键 id，并放在第一列
ALTER TABLE agriculture_data
    ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
ALTER TABLE agriculture_data
    ADD COLUMN nitrogen DOUBLE DEFAULT 0 COMMENT '氮肥',
    ADD COLUMN phosphorus DOUBLE DEFAULT 0 COMMENT '磷肥',
    ADD COLUMN potassium DOUBLE DEFAULT 0 COMMENT '钾肥',
    ADD COLUMN crop_type VARCHAR(50) DEFAULT 'corn' COMMENT '作物类型',
    ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE agriculture_data ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
ALTER TABLE agriculture_data
    ADD COLUMN nitrogen DOUBLE DEFAULT 50.0,
    ADD COLUMN phosphorus DOUBLE DEFAULT 30.0,
    ADD COLUMN potassium DOUBLE DEFAULT 20.0,
    ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP;
-- 1. 创建用户表
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` varchar(64) NOT NULL COMMENT '登录账号',
                            `password` varchar(128) NOT NULL COMMENT '登录密码',
                            `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名或单位名称',
                            `role_code` varchar(32) NOT NULL DEFAULT 'user' COMMENT '角色: admin 或 user',
                            `status` int DEFAULT '1' COMMENT '状态: 1正常 0冻结',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统授权用户表';

-- 2. 插入初始化账号 (密码明文为123456)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role_code`, `status`) VALUES
                                                                                        ('admin', '123456', '超级管理员', 'admin', 1),
                                                                                        ('user1', '123456', '红星农业合作社', 'user', 1);

-- 如果之前建表失败有残留，先删掉它
DROP TABLE IF EXISTS `sys_user`;

-- 1. 创建用户表（一定要从 CREATE 开始复制）
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` varchar(64) NOT NULL COMMENT '登录账号',
                            `password` varchar(128) NOT NULL COMMENT '登录密码',
                            `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名或单位名称',
                            `role_code` varchar(32) NOT NULL DEFAULT 'user' COMMENT '角色: admin 或 user',
                            `status` int DEFAULT '1' COMMENT '状态: 1正常 0冻结',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统授权用户表';

-- 2. 插入初始化测试账号
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role_code`, `status`) VALUES
                                                                                        ('admin', '123456', '超级管理员', 'admin', 1),
                                                                                        ('user1', '123456', '红星农业合作社', 'user', 1);