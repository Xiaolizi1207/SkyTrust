-- SkyTrust 无人机共享租赁区块链平台数据库建表脚本
-- 版本: 1.0.0
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci

-- 注意：本脚本包含所有核心业务表的创建语句
-- 执行前请确保已创建数据库：CREATE DATABASE IF NOT EXISTS `skytrust_dev` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 禁用外键检查，避免依赖顺序问题
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 表：user（用户表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `phone` VARCHAR(20) NULL COMMENT '手机号（唯一）',
  `email` VARCHAR(100) NULL COMMENT '邮箱',
  `real_name` VARCHAR(50) NULL COMMENT '真实姓名',
  `id_card` VARCHAR(20) NULL COMMENT '身份证号',
  `avatar` VARCHAR(500) NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态（0-禁用，1-启用）',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '用户角色（admin-管理员，user-普通用户，pilot-飞行员）',
  `credit_score` INT NOT NULL DEFAULT 100 COMMENT '信用评分（0-100）',
  `wallet_address` VARCHAR(255) NULL COMMENT '钱包地址（区块链地址）',
  `last_login_time` DATETIME NULL COMMENT '最后登录时间',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_username` (`username`),
  UNIQUE INDEX `idx_phone` (`phone`),
  INDEX `idx_status` (`status`),
  INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------
-- 表：device（设备表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `device` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `model` VARCHAR(50) NULL COMMENT '设备型号',
  `serial_number` VARCHAR(100) NOT NULL COMMENT '设备序列号（唯一）',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '设备状态（0-离线，1-在线，2-飞行中，3-维修中，4-已报废）',
  `latitude` DECIMAL(10, 8) NULL COMMENT '当前纬度',
  `longitude` DECIMAL(11, 8) NULL COMMENT '当前经度',
  `altitude` DECIMAL(10, 2) NULL COMMENT '当前高度（米）',
  `battery_level` INT NULL COMMENT '电池电量（百分比）',
  `speed` DECIMAL(10, 2) NULL COMMENT '飞行速度（米/秒）',
  `total_flight_hours` DECIMAL(10, 2) NULL DEFAULT 0.00 COMMENT '飞行总时长（小时）',
  `owner_id` BIGINT NULL COMMENT '设备所有者ID（关联用户表）',
  `rental_price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '租赁价格（元/小时）',
  `insurance_fee` DECIMAL(10, 2) NULL COMMENT '保险费用（元/次）',
  `description` TEXT NULL COMMENT '设备描述',
  `images` TEXT NULL COMMENT '设备图片URL（多个用逗号分隔）',
  `specifications` JSON NULL COMMENT '设备规格JSON',
  `last_online_time` DATETIME NULL COMMENT '最后上线时间',
  `last_maintenance_time` DATETIME NULL COMMENT '最后维护时间',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_serial_number` (`serial_number`),
  INDEX `idx_status` (`status`),
  INDEX `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表（无人机）';

-- -----------------------------------------------------
-- 表：rental_order（租赁订单表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `rental_order` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号（唯一）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联用户表）',
  `device_id` BIGINT NOT NULL COMMENT '设备ID（关联设备表）',
  `start_time` DATETIME NULL COMMENT '租赁开始时间',
  `end_time` DATETIME NULL COMMENT '租赁结束时间',
  `actual_start_time` DATETIME NULL COMMENT '实际开始时间',
  `actual_end_time` DATETIME NULL COMMENT '实际结束时间',
  `duration` DECIMAL(10, 2) NULL COMMENT '租赁时长（小时）',
  `rental_fee` DECIMAL(10, 2) NULL COMMENT '租赁费用（元）',
  `insurance_fee` DECIMAL(10, 2) NULL COMMENT '保险费用（元）',
  `other_fee` DECIMAL(10, 2) NULL COMMENT '其他费用（元）',
  `total_fee` DECIMAL(10, 2) NULL COMMENT '总费用（元）',
  `actual_payment` DECIMAL(10, 2) NULL COMMENT '实际支付金额（元）',
  `payment_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态（0-未支付，1-已支付，2-支付失败，3-已退款）',
  `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态（0-待开始，1-进行中，2-已完成，3-已取消，4-异常）',
  `payment_time` DATETIME NULL COMMENT '支付时间',
  `payment_method` VARCHAR(20) NULL COMMENT '支付方式（alipay-支付宝，wechat-微信，wallet-钱包）',
  `transaction_id` VARCHAR(100) NULL COMMENT '交易号（第三方支付平台）',
  `contract_address` VARCHAR(255) NULL COMMENT '智能合约地址',
  `blockchain_tx_hash` VARCHAR(255) NULL COMMENT '区块链交易哈希',
  `user_rating` INT NULL COMMENT '用户评价（1-5星）',
  `user_comment` TEXT NULL COMMENT '用户评价内容',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_order_no` (`order_no`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_device_id` (`device_id`),
  INDEX `idx_order_status` (`order_status`),
  INDEX `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租赁订单表';

-- -----------------------------------------------------
-- 表：payment（支付记录表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `payment` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `payment_no` VARCHAR(50) NOT NULL COMMENT '支付订单号（唯一）',
  `order_id` BIGINT NOT NULL COMMENT '关联订单ID（rental_order表）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联用户表）',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额（元）',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态（0-待支付，1-支付成功，2-支付失败，3-已退款）',
  `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式（alipay-支付宝，wechat-微信，wallet-钱包）',
  `transaction_id` VARCHAR(100) NULL COMMENT '第三方支付平台交易号',
  `pay_time` DATETIME NULL COMMENT '支付完成时间',
  `remark` VARCHAR(500) NULL COMMENT '支付备注',
  `notify_url` VARCHAR(500) NULL COMMENT '回调通知URL',
  `notify_status` TINYINT NULL DEFAULT 0 COMMENT '回调状态（0-未回调，1-回调成功，2-回调失败）',
  `notify_time` DATETIME NULL COMMENT '回调时间',
  `notify_response` TEXT NULL COMMENT '回调响应内容',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_payment_no` (`payment_no`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- -----------------------------------------------------
-- 表：insurance（保险记录表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `insurance` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `insurance_no` VARCHAR(50) NOT NULL COMMENT '保险单号（唯一）',
  `order_id` BIGINT NOT NULL COMMENT '关联订单ID（rental_order表）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联用户表）',
  `device_id` BIGINT NOT NULL COMMENT '设备ID（关联设备表）',
  `insurance_type` TINYINT NOT NULL DEFAULT 0 COMMENT '保险类型（0-基础保险，1-高级保险，2-全险）',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '保险金额（元）',
  `rate` DECIMAL(5, 4) NULL COMMENT '保险费率（百分比）',
  `start_time` DATETIME NULL COMMENT '保险开始时间',
  `end_time` DATETIME NULL COMMENT '保险结束时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '保险状态（0-未生效，1-生效中，2-已过期，3-已理赔）',
  `claim_amount` DECIMAL(10, 2) NULL COMMENT '理赔金额（元）',
  `claim_time` DATETIME NULL COMMENT '理赔时间',
  `claim_reason` VARCHAR(500) NULL COMMENT '理赔原因',
  `claim_status` TINYINT NULL DEFAULT 0 COMMENT '理赔状态（0-未理赔，1-申请中，2-已赔付，3-已拒绝）',
  `terms` JSON NULL COMMENT '保险条款JSON',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_insurance_no` (`insurance_no`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保险记录表';

-- -----------------------------------------------------
-- 表：blockchain_transaction（区块链交易记录表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `blockchain_transaction` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tx_hash` VARCHAR(255) NOT NULL COMMENT '交易哈希（唯一）',
  `contract_address` VARCHAR(255) NULL COMMENT '智能合约地址',
  `block_number` BIGINT NULL COMMENT '区块号',
  `block_hash` VARCHAR(255) NULL COMMENT '区块哈希',
  `from_address` VARCHAR(255) NOT NULL COMMENT '交易发起方地址',
  `to_address` VARCHAR(255) NOT NULL COMMENT '交易接收方地址',
  `amount` DECIMAL(30, 18) NULL COMMENT '交易金额（ETH/代币数量）',
  `gas_fee` DECIMAL(30, 18) NULL COMMENT '交易gas费用',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '交易状态（0-待确认，1-已确认，2-失败）',
  `confirm_time` DATETIME NULL COMMENT '确认时间',
  `tx_type` TINYINT NULL COMMENT '交易类型（0-租赁合约创建，1-支付，2-保险，3-设备状态更新）',
  `business_id` BIGINT NULL COMMENT '关联业务ID（如订单ID、支付ID等）',
  `business_type` VARCHAR(50) NULL COMMENT '关联业务类型（order-订单，payment-支付，insurance-保险）',
  `input_data` JSON NULL COMMENT '交易输入数据（JSON格式）',
  `receipt` JSON NULL COMMENT '交易收据（JSON格式）',
  `chain_id` INT NULL COMMENT '网络ID（链ID）',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_tx_hash` (`tx_hash`),
  INDEX `idx_contract_address` (`contract_address`),
  INDEX `idx_from_address` (`from_address`),
  INDEX `idx_business` (`business_type`, `business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区块链交易记录表';

-- -----------------------------------------------------
-- 表：device_track（设备位置轨迹表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `device_track` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID（关联设备表）',
  `order_id` BIGINT NULL COMMENT '订单ID（关联租赁订单表，可为空）',
  `latitude` DECIMAL(10, 8) NOT NULL COMMENT '纬度',
  `longitude` DECIMAL(11, 8) NOT NULL COMMENT '经度',
  `altitude` DECIMAL(10, 2) NULL COMMENT '高度（米）',
  `speed` DECIMAL(10, 2) NULL COMMENT '速度（米/秒）',
  `heading` DECIMAL(5, 2) NULL COMMENT '方向角度（0-360度）',
  `battery_level` INT NULL COMMENT '电池电量（百分比）',
  `signal_strength` INT NULL COMMENT '信号强度（0-100）',
  `satellite_count` INT NULL COMMENT '卫星数量',
  `accuracy` DECIMAL(10, 2) NULL COMMENT '定位精度（米）',
  `source` VARCHAR(20) NULL COMMENT '位置来源（gps-卫星定位，network-网络定位，fused-融合定位）',
  `record_time` DATETIME NOT NULL COMMENT '记录时间',
  `is_abnormal` TINYINT(1) NULL DEFAULT 0 COMMENT '是否为异常位置（0-正常，1-异常）',
  `abnormal_reason` VARCHAR(500) NULL COMMENT '异常原因',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `idx_device_id` (`device_id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_record_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备位置轨迹表';

-- -----------------------------------------------------
-- 表：flight_record（飞行记录表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `flight_record` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `flight_no` VARCHAR(50) NOT NULL COMMENT '飞行记录编号（唯一）',
  `order_id` BIGINT NOT NULL COMMENT '订单ID（关联租赁订单表）',
  `device_id` BIGINT NOT NULL COMMENT '设备ID（关联设备表）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联用户表）',
  `start_time` DATETIME NULL COMMENT '飞行开始时间',
  `end_time` DATETIME NULL COMMENT '飞行结束时间',
  `actual_start_time` DATETIME NULL COMMENT '实际开始时间',
  `actual_end_time` DATETIME NULL COMMENT '实际结束时间',
  `duration` INT NULL COMMENT '飞行时长（分钟）',
  `distance` DECIMAL(10, 2) NULL COMMENT '飞行距离（米）',
  `max_altitude` DECIMAL(10, 2) NULL COMMENT '最大高度（米）',
  `max_speed` DECIMAL(10, 2) NULL COMMENT '最大速度（米/秒）',
  `avg_speed` DECIMAL(10, 2) NULL COMMENT '平均速度（米/秒）',
  `takeoff_latitude` DECIMAL(10, 8) NULL COMMENT '起飞地点纬度',
  `takeoff_longitude` DECIMAL(11, 8) NULL COMMENT '起飞地点经度',
  `landing_latitude` DECIMAL(10, 8) NULL COMMENT '降落地点纬度',
  `landing_longitude` DECIMAL(11, 8) NULL COMMENT '降落地点经度',
  `flight_status` TINYINT NOT NULL DEFAULT 0 COMMENT '飞行状态（0-准备中，1-飞行中，2-已完成，3-已取消，4-异常终止）',
  `weather_info` JSON NULL COMMENT '天气状况JSON',
  `flight_path` JSON NULL COMMENT '飞行路径数据（JSON格式的轨迹点数组）',
  `flight_params` JSON NULL COMMENT '飞行参数JSON（如风速、温度等）',
  `incidents` JSON NULL COMMENT '异常事件记录JSON',
  `ai_analysis` JSON NULL COMMENT 'AI分析结果JSON',
  `flight_score` INT NULL COMMENT '飞行评分（1-10分）',
  `is_violation` TINYINT(1) NULL DEFAULT 0 COMMENT '是否违规（0-否，1-是）',
  `violation_reason` VARCHAR(500) NULL COMMENT '违规原因',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_flight_no` (`flight_no`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_device_id` (`device_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_flight_status` (`flight_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='飞行记录表';

-- -----------------------------------------------------
-- 表：smart_contract（智能合约表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `smart_contract` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `contract_name` VARCHAR(100) NOT NULL COMMENT '合约名称',
  `contract_address` VARCHAR(255) NOT NULL COMMENT '合约地址（唯一）',
  `contract_abi` JSON NULL COMMENT '合约ABI（JSON格式）',
  `contract_bytecode` TEXT NULL COMMENT '合约字节码',
  `contract_type` TINYINT NOT NULL DEFAULT 0 COMMENT '合约类型（0-租赁合约，1-支付合约，2-保险合约，3-设备合约）',
  `version` VARCHAR(20) NULL COMMENT '合约版本',
  `deploy_tx_hash` VARCHAR(255) NULL COMMENT '部署交易哈希',
  `deployer_address` VARCHAR(255) NULL COMMENT '部署者地址',
  `chain_id` INT NULL COMMENT '部署网络ID（链ID）',
  `deploy_time` DATETIME NULL COMMENT '部署时间',
  `contract_status` TINYINT NOT NULL DEFAULT 0 COMMENT '合约状态（0-未激活，1-已激活，2-已暂停，3-已终止）',
  `upgradable` TINYINT(1) NULL DEFAULT 0 COMMENT '是否可升级（0-否，1-是）',
  `upgrade_address` VARCHAR(255) NULL COMMENT '升级合约地址',
  `owner_address` VARCHAR(255) NULL COMMENT '合约拥有者地址',
  `description` TEXT NULL COMMENT '合约描述',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_contract_address` (`contract_address`),
  INDEX `idx_contract_type` (`contract_type`),
  INDEX `idx_contract_status` (`contract_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能合约表';

-- -----------------------------------------------------
-- 表：system_config（系统配置表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT NULL COMMENT '配置值',
  `config_type` TINYINT NOT NULL DEFAULT 0 COMMENT '配置类型（0-系统配置，1-业务配置，2-区块链配置，3-AI配置，4-物联网配置）',
  `description` VARCHAR(500) NULL COMMENT '配置描述',
  `modifiable` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可修改（0-否，1-是）',
  `last_modified_time` DATETIME NULL COMMENT '最后修改时间',
  `last_modified_by` BIGINT NULL COMMENT '最后修改人',
  `config_group` VARCHAR(50) NULL COMMENT '配置分组',
  `sort_order` INT NULL DEFAULT 0 COMMENT '排序号',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_config_key` (`config_key`),
  INDEX `idx_config_type` (`config_type`),
  INDEX `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- -----------------------------------------------------
-- 表：sys_menu（系统菜单表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID（0表示根菜单）',
  `menu_name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
  `menu_code` VARCHAR(100) NOT NULL COMMENT '菜单代码（唯一），用于权限标识',
  `menu_path` VARCHAR(500) NULL COMMENT '前端路由路径',
  `component` VARCHAR(500) NULL COMMENT '组件路径',
  `icon` VARCHAR(100) NULL COMMENT '菜单图标',
  `menu_type` TINYINT NOT NULL DEFAULT 1 COMMENT '菜单类型（1-目录，2-菜单，3-按钮）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `perms` VARCHAR(500) NULL COMMENT '权限标识（如sys:user:query）',
  `is_external` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否外链（0-否，1-是）',
  `external_url` VARCHAR(500) NULL COMMENT '外链地址',
  `is_cache` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否缓存（0-否，1-是）',
  `is_visible` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可见（0-隐藏，1-显示）',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_menu_code` (`menu_code`),
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_menu_type` (`menu_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';

-- -----------------------------------------------------
-- 表：sys_role（系统角色表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色代码（唯一），与UserRoleEnum保持兼容',
  `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(500) NULL COMMENT '角色描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_role_code` (`role_code`),
  INDEX `idx_status` (`status`),
  INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- -----------------------------------------------------
-- 表：sys_role_menu（角色菜单关联表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_role_menu` (`role_id`, `menu_id`),
  INDEX `idx_role_id` (`role_id`),
  INDEX `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- -----------------------------------------------------
-- 表：sys_user_role（用户角色关联表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_role` (`user_id`, `role_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- -----------------------------------------------------
-- 初始化系统配置数据
-- -----------------------------------------------------
INSERT IGNORE INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `description`, `modifiable`, `config_group`, `sort_order`, `create_time`, `update_time`, `deleted`) VALUES
(1, 'system.name', 'SkyTrust无人机共享租赁区块链平台', 0, '系统名称', 0, 'system', 1, NOW(), NOW(), 0),
(2, 'system.version', '1.0.0', 0, '系统版本', 0, 'system', 2, NOW(), NOW(), 0),
(3, 'system.copyright', 'Copyright © 2026 SkyTrust团队', 0, '版权信息', 0, 'system', 3, NOW(), NOW(), 0),
(4, 'blockchain.enabled', 'true', 2, '是否启用区块链', 1, 'blockchain', 1, NOW(), NOW(), 0),
(5, 'blockchain.node_url', 'http://localhost:8545', 2, '区块链节点URL', 1, 'blockchain', 2, NOW(), NOW(), 0),
(6, 'blockchain.chain_id', '1337', 2, '区块链链ID', 1, 'blockchain', 3, NOW(), NOW(), 0),
(7, 'ai.enabled', 'true', 3, '是否启用AI服务', 1, 'ai', 1, NOW(), NOW(), 0),
(8, 'ai.service_url', 'http://localhost:5000', 3, 'AI服务URL', 1, 'ai', 2, NOW(), NOW(), 0),
(9, 'iot.mqtt.broker_url', 'tcp://localhost:1883', 4, 'MQTT Broker地址', 1, 'iot', 1, NOW(), NOW(), 0),
(10, 'iot.mqtt.username', 'skytrust', 4, 'MQTT用户名', 1, 'iot', 2, NOW(), NOW(), 0),
(11, 'iot.mqtt.password', 'skytrust123', 4, 'MQTT密码', 1, 'iot', 3, NOW(), NOW(), 0),
(12, 'rental.default_price', '100.00', 1, '默认租赁价格（元/小时）', 1, 'rental', 1, NOW(), NOW(), 0),
(13, 'rental.default_insurance_fee', '10.00', 1, '默认保险费（元/次）', 1, 'rental', 2, NOW(), NOW(), 0),
(14, 'user.default_credit_score', '100', 1, '用户默认信用评分', 1, 'user', 1, NOW(), NOW(), 0),
(15, 'upload.max_file_size', '10485760', 0, '最大文件上传大小（字节）', 1, 'upload', 1, NOW(), NOW(), 0);

-- -----------------------------------------------------
-- wallet：user 表添加余额字段（仅当列不存在时执行）
-- -----------------------------------------------------
SET @stmt = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'balance') = 0,
  'ALTER TABLE `user` ADD COLUMN `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT ''钱包余额（元）'' AFTER `wallet_address`',
  'SELECT ''Column balance already exists'' AS msg'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------
-- 表：wallet_transaction（钱包交易记录表）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL COMMENT '交易类型（0-充值，1-消费，2-退款，3-提现）',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '交易金额（元）',
  `balance_before` DECIMAL(10,2) NOT NULL COMMENT '交易前余额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '交易后余额',
  `description` VARCHAR(255) NULL COMMENT '交易描述',
  `order_id` BIGINT NULL COMMENT '关联订单ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '交易状态（0-成功，1-失败，2-处理中）',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `idx_wallet_user_id` (`user_id`),
  INDEX `idx_wallet_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包交易记录表';

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 建表完成提示
SELECT 'SkyTrust 数据库表创建完成！' AS 'Message';