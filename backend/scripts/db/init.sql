-- SkyTrust无人机共享租赁区块链平台 - 数据库初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2026-03-31

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `skytrust_dev` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `skytrust_dev`;

-- 设置SQL模式
SET sql_mode = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ========== 用户相关表 ==========

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `credit_score` INT DEFAULT 100 COMMENT '信用分(0-100)',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额',
  `deposit_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '押金金额',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常, 2-冻结',
  `user_type` TINYINT DEFAULT 1 COMMENT '用户类型: 1-普通用户, 2-商家, 3-运维, 4-监管',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_credit_score` (`credit_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========== 设备相关表 ==========

-- 无人机设备表
CREATE TABLE IF NOT EXISTS `device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_sn` VARCHAR(50) NOT NULL COMMENT '设备序列号',
  `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `device_type` VARCHAR(50) NOT NULL COMMENT '设备类型',
  `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
  `model` VARCHAR(50) DEFAULT NULL COMMENT '型号',
  `owner_id` BIGINT NOT NULL COMMENT '所属商家ID',
  `current_status` TINYINT DEFAULT 1 COMMENT '当前状态: 1-空闲, 2-租赁中, 3-维护中, 4-已下线',
  `battery_level` INT DEFAULT 100 COMMENT '电池电量(0-100)',
  `gps_latitude` DECIMAL(10,8) DEFAULT NULL COMMENT 'GPS纬度',
  `gps_longitude` DECIMAL(11,8) DEFAULT NULL COMMENT 'GPS经度',
  `last_online_time` DATETIME DEFAULT NULL COMMENT '最后在线时间',
  `rental_price_hour` DECIMAL(8,2) NOT NULL COMMENT '租赁价格(元/小时)',
  `deposit_amount` DECIMAL(10,2) NOT NULL COMMENT '押金金额',
  `total_rental_hours` INT DEFAULT 0 COMMENT '累计租赁小时数',
  `total_income` DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计收入',
  `device_specs` JSON DEFAULT NULL COMMENT '设备规格(JSON)',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_sn` (`device_sn`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_current_status` (`current_status`),
  KEY `idx_gps_location` (`gps_latitude`, `gps_longitude`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='无人机设备表';

-- ========== 租赁相关表 ==========

-- 租赁订单表
CREATE TABLE IF NOT EXISTS `rental_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `start_time` DATETIME NOT NULL COMMENT '租赁开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '租赁结束时间',
  `actual_end_time` DATETIME DEFAULT NULL COMMENT '实际结束时间',
  `total_hours` DECIMAL(5,2) DEFAULT NULL COMMENT '租赁总小时数',
  `hourly_price` DECIMAL(8,2) NOT NULL COMMENT '小时单价',
  `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '订单总金额',
  `deposit_amount` DECIMAL(10,2) NOT NULL COMMENT '押金金额',
  `payment_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '实际支付金额',
  `order_status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态: 1-待支付, 2-已支付, 3-租赁中, 4-已完成, 5-已取消, 6-已退款',
  `payment_status` TINYINT DEFAULT 0 COMMENT '支付状态: 0-未支付, 1-已支付, 2-已退款',
  `start_location` VARCHAR(200) DEFAULT NULL COMMENT '起始位置',
  `end_location` VARCHAR(200) DEFAULT NULL COMMENT '结束位置',
  `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
  `blockchain_tx_hash` VARCHAR(100) DEFAULT NULL COMMENT '区块链交易哈希',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_blockchain_tx` (`blockchain_tx_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租赁订单表';

-- ========== 支付相关表 ==========

-- 支付记录表
CREATE TABLE IF NOT EXISTS `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `payment_no` VARCHAR(50) NOT NULL COMMENT '支付流水号',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `payment_type` TINYINT NOT NULL COMMENT '支付类型: 1-微信支付, 2-支付宝, 3-余额支付',
  `payment_amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `payment_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态: 0-待支付, 1-支付成功, 2-支付失败, 3-已退款',
  `third_party_trade_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易号',
  `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `refund_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '退款金额',
  `payment_data` JSON DEFAULT NULL COMMENT '支付数据(JSON)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_payment_time` (`payment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ========== 区块链相关表 ==========

-- 区块链交易记录表
CREATE TABLE IF NOT EXISTS `blockchain_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `tx_hash` VARCHAR(100) NOT NULL COMMENT '交易哈希',
  `tx_type` VARCHAR(50) NOT NULL COMMENT '交易类型: rental_create, rental_complete, payment, etc.',
  `business_id` BIGINT NOT NULL COMMENT '业务ID',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型: rental_order, payment_record, etc.',
  `from_address` VARCHAR(100) DEFAULT NULL COMMENT '发送方地址',
  `to_address` VARCHAR(100) DEFAULT NULL COMMENT '接收方地址',
  `amount` DECIMAL(20,8) DEFAULT NULL COMMENT '金额(ETH)',
  `gas_used` BIGINT DEFAULT NULL COMMENT 'Gas消耗',
  `block_number` BIGINT DEFAULT NULL COMMENT '区块号',
  `block_hash` VARCHAR(100) DEFAULT NULL COMMENT '区块哈希',
  `confirmations` INT DEFAULT 0 COMMENT '确认数',
  `tx_status` TINYINT DEFAULT 0 COMMENT '交易状态: 0-待确认, 1-已确认, 2-失败',
  `tx_data` JSON DEFAULT NULL COMMENT '交易数据(JSON)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tx_hash` (`tx_hash`),
  KEY `idx_business` (`business_type`, `business_id`),
  KEY `idx_tx_type` (`tx_type`),
  KEY `idx_tx_status` (`tx_status`),
  KEY `idx_block_number` (`block_number`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区块链交易记录表';

-- ========== 系统相关表 ==========

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `user_type` TINYINT DEFAULT NULL COMMENT '用户类型',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_desc` VARCHAR(500) NOT NULL COMMENT '操作描述',
  `request_url` VARCHAR(500) NOT NULL COMMENT '请求URL',
  `request_method` VARCHAR(10) NOT NULL COMMENT '请求方法',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_data` TEXT DEFAULT NULL COMMENT '响应数据',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `execute_time` BIGINT DEFAULT NULL COMMENT '执行时间(毫秒)',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-失败, 1-成功',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_desc` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
  `config_type` TINYINT DEFAULT 1 COMMENT '配置类型: 1-系统配置, 2-业务配置',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_type` (`config_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ========== 初始化数据 ==========

-- 插入默认管理员用户 (密码: skytrust123)
INSERT INTO `user` (
  `username`, `phone`, `email`, `password_hash`, `real_name`,
  `credit_score`, `balance`, `user_type`, `status`
) VALUES (
  'admin',
  '13800138000',
  'admin@skytrust.com',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6F2T5U9VpB9C8X8UqQ6Jq1YpW6', -- bcrypt加密的'skytrust123'
  '系统管理员',
  100,
  10000.00,
  3,
  1
) ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- 插入默认商家用户
INSERT INTO `user` (
  `username`, `phone`, `email`, `password_hash`, `real_name`,
  `credit_score`, `balance`, `user_type`, `status`
) VALUES (
  'merchant1',
  '13900139001',
  'merchant1@skytrust.com',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6F2T5U9VpB9C8X8UqQ6Jq1YpW6',
  '商家一号',
  95,
  5000.00,
  2,
  1
) ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- 插入系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`, `config_type`) VALUES
('platform.name', 'SkyTrust无人机共享租赁平台', '平台名称', 1),
('platform.version', '1.0.0', '平台版本', 1),
('rental.max_hours', '24', '单次最大租赁小时数', 2),
('rental.min_hours', '0.5', '单次最小租赁小时数', 2),
('deposit.rate', '0.2', '押金比例', 2),
('risk.min_credit_score', '60', '最低信用分要求', 2),
('sms.enabled', 'false', '短信验证码是否启用', 1),
('blockchain.enabled', 'true', '区块链是否启用', 1)
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `update_time` = CURRENT_TIMESTAMP;

-- 创建视图
CREATE OR REPLACE VIEW `v_device_status` AS
SELECT
  d.id,
  d.device_sn,
  d.device_name,
  d.device_type,
  d.current_status,
  d.battery_level,
  d.gps_latitude,
  d.gps_longitude,
  d.last_online_time,
  d.rental_price_hour,
  u.username AS owner_name,
  u.phone AS owner_phone,
  CASE
    WHEN d.current_status = 1 THEN '空闲'
    WHEN d.current_status = 2 THEN '租赁中'
    WHEN d.current_status = 3 THEN '维护中'
    WHEN d.current_status = 4 THEN '已下线'
    ELSE '未知'
  END AS status_text,
  TIMESTAMPDIFF(MINUTE, d.last_online_time, NOW()) AS offline_minutes
FROM `device` d
LEFT JOIN `user` u ON d.owner_id = u.id
WHERE d.deleted = 0 AND d.status = 1;

-- 创建存储过程：获取用户租赁统计
DELIMITER //
CREATE PROCEDURE `sp_get_user_rental_stats`(IN user_id BIGINT)
BEGIN
  SELECT
    COUNT(*) AS total_orders,
    SUM(total_amount) AS total_spent,
    AVG(total_hours) AS avg_rental_hours,
    MAX(create_time) AS last_rental_time
  FROM `rental_order`
  WHERE user_id = user_id
    AND order_status IN (3, 4) -- 租赁中或已完成
    AND deleted = 0;
END //
DELIMITER ;

-- 创建函数：计算设备使用率
DELIMITER //
CREATE FUNCTION `fn_calculate_device_utilization`(device_id BIGINT, start_date DATE, end_date DATE)
RETURNS DECIMAL(5,2)
DETERMINISTIC
BEGIN
  DECLARE total_hours DECIMAL(10,2);
  DECLARE available_hours DECIMAL(10,2);
  DECLARE utilization_rate DECIMAL(5,2);

  -- 计算该时间段内的总租赁小时数
  SELECT COALESCE(SUM(total_hours), 0) INTO total_hours
  FROM `rental_order`
  WHERE device_id = device_id
    AND DATE(create_time) BETWEEN start_date AND end_date
    AND order_status IN (3, 4)
    AND deleted = 0;

  -- 计算可用小时数（假设每天24小时可用）
  SET available_hours = DATEDIFF(end_date, start_date) * 24;

  -- 计算使用率
  IF available_hours > 0 THEN
    SET utilization_rate = (total_hours / available_hours) * 100;
  ELSE
    SET utilization_rate = 0;
  END IF;

  RETURN LEAST(utilization_rate, 100); -- 确保不超过100%
END //
DELIMITER ;

-- 创建事件：每天清理过期日志
DELIMITER //
CREATE EVENT `ev_clean_old_logs`
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
  -- 删除30天前的操作日志
  DELETE FROM `operation_log`
  WHERE create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

  -- 记录清理日志
  INSERT INTO `operation_log` (
    `operation_type`, `operation_desc`, `request_url`, `request_method`
  ) VALUES (
    'SYSTEM_CLEANUP',
    '自动清理过期日志',
    'EVENT',
    'SCHEDULE'
  );
END //
DELIMITER ;

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;

-- ========== 权限配置 ==========

-- 创建数据库用户（在生产环境中使用）
-- CREATE USER 'skytrust_app'@'%' IDENTIFIED BY 'StrongPassword123!';
-- GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `skytrust_dev`.* TO 'skytrust_app'@'%';
-- FLUSH PRIVILEGES;

COMMIT;

-- 完成消息
SELECT 'SkyTrust数据库初始化完成!' AS message;