-- ================================================
-- 智学伴 - 数据库初始化脚本
-- 数据库: MySQL 8.0
-- 字符集: utf8mb4
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `study_companion` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `study_companion`;

-- ================================================
-- 1. 用户表 (user)
-- ================================================
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role` VARCHAR(20) DEFAULT 'user' COMMENT '用户角色(user/admin)',
  `daily_goal` INT DEFAULT 120 COMMENT '每日目标时长(分钟)',
  `weekly_goal` INT DEFAULT 840 COMMENT '每周目标时长(分钟)',
  `monthly_goal` INT DEFAULT 3600 COMMENT '每月目标时长(分钟)',
  `dark_mode` TINYINT(1) DEFAULT 0 COMMENT '深色模式(0关闭 1开启)',
  `notification_enabled` TINYINT(1) DEFAULT 1 COMMENT '通知开关(0关闭 1开启)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ================================================
-- 2. 科目表 (subject)
-- ================================================
CREATE TABLE `subject` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '科目名称',
  `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '科目颜色',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '科目图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_preset` TINYINT(1) DEFAULT 0 COMMENT '是否预设科目(0否 1是)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科目表';

-- ================================================
-- 3. 学习记录表 (study_record)
-- ================================================
CREATE TABLE `study_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `subject_id` BIGINT NOT NULL COMMENT '科目ID',
  `study_date` DATE NOT NULL COMMENT '学习日期',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `duration` INT NOT NULL COMMENT '学习时长(分钟)',
  `remark` TEXT COMMENT '备注(AI生成)',
  `mood` VARCHAR(50) DEFAULT NULL COMMENT '心情(用户自定义)',
  `focus_level` TINYINT DEFAULT NULL COMMENT '专注度(1-5 用户自评)',
  `ai_focus_level` TINYINT DEFAULT NULL COMMENT 'AI判断的专注度(1-5)',
  `interruption_count` INT DEFAULT 0 COMMENT '中断次数',
  `interruption_reason` TEXT COMMENT '中断原因',
  `ai_allow_modify` TINYINT DEFAULT NULL COMMENT 'AI是否允许修改(0不允许 1允许)',
  `ai_modify_reason` TEXT COMMENT 'AI修改判断原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_subject_id` (`subject_id`),
  KEY `idx_study_date` (`study_date`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`subject_id`) REFERENCES `subject`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习记录表';

-- ================================================
-- 4. 打卡表 (check_in)
-- ================================================
CREATE TABLE `check_in` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `check_date` DATE NOT NULL COMMENT '打卡日期',
  `total_duration` INT DEFAULT 0 COMMENT '当天总学习时长(分钟)',
  `is_completed` TINYINT(1) DEFAULT 0 COMMENT '是否完成打卡(0未完成 1完成)',
  `streak` INT DEFAULT 0 COMMENT '当前连续打卡天数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `check_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_check_date` (`check_date`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡表';

-- ================================================
-- 5. 断签记录表 (miss_record)
-- ================================================
CREATE TABLE `miss_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `miss_date` DATE NOT NULL COMMENT '断签日期',
  `reason` TEXT NOT NULL COMMENT '断签原因',
  `ai_allow_replenish` TINYINT(1) DEFAULT NULL COMMENT 'AI判断是否允许补签(0不允许 1允许)',
  `is_replenished` TINYINT(1) DEFAULT 0 COMMENT '是否已补签(0未补签 1已补签)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_miss_date` (`miss_date`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='断签记录表';

-- ================================================
-- 6. 目标表 (goal)
-- ================================================
CREATE TABLE `goal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `goal_type` TINYINT NOT NULL COMMENT '目标类型(1每日 2每周 3每月)',
  `goal_value` INT NOT NULL COMMENT '目标值(分钟)',
  `current_value` INT DEFAULT 0 COMMENT '当前值(分钟)',
  `goal_date` DATE DEFAULT NULL COMMENT '目标日期',
  `is_completed` TINYINT(1) DEFAULT 0 COMMENT '是否完成(0未完成 1完成)',
  `ai_suggested` TINYINT(1) DEFAULT 0 COMMENT '是否AI推荐(0否 1是)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_goal_type` (`goal_type`),
  KEY `idx_goal_date` (`goal_date`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='目标表';

-- ================================================
-- 7. 日记表 (diary)
-- ================================================
CREATE TABLE `diary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `diary_date` DATE NOT NULL COMMENT '日记日期',
  `content` TEXT NOT NULL COMMENT '日记内容(Markdown)',
  `summary` TEXT COMMENT '今日总结',
  `plan` TEXT COMMENT '明日计划',
  `reflection` TEXT COMMENT '心得体会',
  `problems` TEXT COMMENT '问题记录',
  `ai_generated` TINYINT(1) DEFAULT 0 COMMENT '是否AI生成(0否 1是)',
  `ai_generate_count` INT DEFAULT 0 COMMENT 'AI生成次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `diary_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_diary_date` (`diary_date`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记表';

-- ================================================
-- 8. 日记图片表 (diary_image)
-- ================================================
CREATE TABLE `diary_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `diary_id` BIGINT NOT NULL COMMENT '日记ID',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `image_size` INT DEFAULT NULL COMMENT '图片大小(KB)',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_diary_id` (`diary_id`),
  FOREIGN KEY (`diary_id`) REFERENCES `diary`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记图片表';

-- ================================================
-- 9. 用户统计表 (user_statistics)
-- ================================================
CREATE TABLE `user_statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_days` INT DEFAULT 0 COMMENT '累计学习天数',
  `total_duration` INT DEFAULT 0 COMMENT '累计学习时长(分钟)',
  `current_streak` INT DEFAULT 0 COMMENT '当前连续打卡天数',
  `max_streak` INT DEFAULT 0 COMMENT '最长连续打卡天数',
  `last_check_date` DATE DEFAULT NULL COMMENT '最后打卡日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';

-- ================================================
-- 10. AI分析记录表 (ai_analysis)
-- ================================================
CREATE TABLE `ai_analysis` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `analysis_type` VARCHAR(50) NOT NULL COMMENT '分析类型(weekly_report/monthly_report/advice/diary)',
  `content` TEXT NOT NULL COMMENT '分析内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_analysis_type` (`analysis_type`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI分析记录表';

-- ================================================
-- 11. AI聊天历史表 (ai_chat_history)
-- ================================================
CREATE TABLE `ai_chat_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role` VARCHAR(20) NOT NULL COMMENT '消息角色(user/assistant)',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天历史表';

-- ================================================
-- 12. 考试表 (exam)
-- ================================================
CREATE TABLE `exam` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '考试名称',
  `exam_date` DATE NOT NULL COMMENT '考试日期',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '考试地点',
  `description` TEXT DEFAULT NULL COMMENT '考试描述',
  `is_target` TINYINT(1) DEFAULT 0 COMMENT '是否目标考试(0否 1是)',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_exam_date` (`exam_date`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试表';

-- ================================================
-- 13. 学习资料表 (learning_material)
-- ================================================
CREATE TABLE `learning_material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `subject_id` BIGINT DEFAULT NULL COMMENT '科目ID',
  `title` VARCHAR(200) NOT NULL COMMENT '资料标题',
  `description` TEXT DEFAULT NULL COMMENT '资料描述',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件URL',
  `file_name` VARCHAR(200) NOT NULL COMMENT '原始文件名',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` VARCHAR(20) DEFAULT NULL COMMENT '文件类型(pdf,doc等)',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签(逗号分隔)',
  `is_favorite` TINYINT(1) DEFAULT 0 COMMENT '是否收藏(0否 1是)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_subject_id` (`subject_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`subject_id`) REFERENCES `subject`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习资料表';

-- ================================================
-- 14. 系统配置表 (system_config)
-- ================================================
CREATE TABLE `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `description` VARCHAR(255) COMMENT '配置说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('ai.api-key', '', 'AI API密钥'),
('ai.base-url', 'https://token-plan-sgp.xiaomimimo.com/v1', 'AI接口地址'),
('ai.model', 'mimo-v2.5-pro', 'AI模型名称'),
('ai.enabled', 'true', 'AI功能开关'),
('system.upload-max-size', '10485760', '上传文件大小限制(字节)'),
('system.ai-generate-limit', '3', 'AI日记生成次数限制');

-- ================================================
-- 初始化数据：预设科目模板
-- ================================================
-- 注意：预设科目在用户注册时自动创建，这里只是记录预设模板
-- 实际数据会在用户注册时插入到 subject 表中

-- 预设科目模板（供程序使用，不直接插入数据库）
-- 1. 行测 - 颜色 #409EFF - 图标 icon-xingce
-- 2. 申论 - 颜色 #67C23A - 图标 icon-shenlun
-- 3. 公共基础知识 - 颜色 #E6A23C - 图标 icon-gonggong
