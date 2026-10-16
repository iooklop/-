-- 资源共享平台数据库初始化脚本
-- 使用 CREATE TABLE IF NOT EXISTS，可重复执行且不破坏已有数据

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '账号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `name` VARCHAR(50) COMMENT '姓名',
    `avatar` VARCHAR(255) COMMENT '头像',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `age` INT COMMENT '年龄',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_role` (`role`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` INT DEFAULT 0 COMMENT '父分类ID，0为顶级',
    `icon` VARCHAR(255) COMMENT '图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_parent` (`parent_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源分类表';

-- 资源表
CREATE TABLE IF NOT EXISTS `resource` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '资源ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `description` TEXT COMMENT '描述',
    `category_id` INT COMMENT '分类ID',
    `cover_image` VARCHAR(500) COMMENT '封面图路径',
    `file_name` VARCHAR(255) COMMENT '原始文件名',
    `file_path` VARCHAR(500) COMMENT '文件存储路径',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) COMMENT '文件类型/扩展名',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` INT DEFAULT 0 COMMENT '收藏次数',
    `like_count` INT DEFAULT 0 COMMENT '点赞次数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `points` INT DEFAULT 0 COMMENT '下载所需积分',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
    `reject_reason` VARCHAR(500) COMMENT '驳回原因',
    `uploader_id` INT COMMENT '上传者ID',
    `is_recommended` TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category` (`category_id`),
    INDEX `idx_uploader` (`uploader_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `resource_id` INT NOT NULL COMMENT '资源ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY `uk_user_resource` (`user_id`, `resource_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 点赞表
CREATE TABLE IF NOT EXISTS `resource_like` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `resource_id` INT NOT NULL COMMENT '资源ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY `uk_user_resource` (`user_id`, `resource_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    `resource_id` INT NOT NULL COMMENT '资源ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `parent_id` INT DEFAULT 0 COMMENT '父评论ID，0为顶级评论',
    `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    INDEX `idx_resource` (`resource_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 下载记录表
CREATE TABLE IF NOT EXISTS `download_record` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `resource_id` INT NOT NULL COMMENT '资源ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
    INDEX `idx_user` (`user_id`),
    INDEX `idx_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='下载记录表';

-- 打赏记录表
CREATE TABLE IF NOT EXISTS `reward` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '打赏ID',
    `order_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '商户订单号',
    `user_id` INT NOT NULL COMMENT '打赏人ID',
    `resource_id` INT NOT NULL COMMENT '被打赏资源ID',
    `uploader_id` INT NOT NULL COMMENT '被打赏作者ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '打赏金额(元)',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/PAID/CLOSED',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time` DATETIME COMMENT '支付时间',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打赏记录表';
