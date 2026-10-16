-- 资源共享平台数据库初始化脚本（PostgreSQL 版）
-- 使用 CREATE TABLE IF NOT EXISTS，可重复执行且不破坏已有数据
-- 注意：user 是 PostgreSQL 保留字，需用双引号包裹；Hibernate 侧已开启 globally_quoted_identifiers

-- 用户表
CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    name VARCHAR(50),
    avatar VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(100),
    age INTEGER,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_username ON "user" (username);
CREATE INDEX IF NOT EXISTS idx_role ON "user" (role);
CREATE INDEX IF NOT EXISTS idx_status ON "user" (status);

-- 分类表
CREATE TABLE IF NOT EXISTS category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id INTEGER DEFAULT 0,
    icon VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_parent ON category (parent_id);
CREATE INDEX IF NOT EXISTS idx_status ON category (status);

-- 资源表
CREATE TABLE IF NOT EXISTS resource (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category_id INTEGER,
    cover_image VARCHAR(500),
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    file_size BIGINT DEFAULT 0,
    file_type VARCHAR(50),
    download_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    favorite_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    points INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    reject_reason VARCHAR(500),
    uploader_id INTEGER,
    is_recommended BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_category ON resource (category_id);
CREATE INDEX IF NOT EXISTS idx_uploader ON resource (uploader_id);
CREATE INDEX IF NOT EXISTS idx_status ON resource (status);
CREATE INDEX IF NOT EXISTS idx_create_time ON resource (create_time);

-- 收藏表
CREATE TABLE IF NOT EXISTS favorite (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    resource_id INTEGER NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_favorite_user_resource UNIQUE (user_id, resource_id)
);
CREATE INDEX IF NOT EXISTS idx_fav_user ON favorite (user_id);
CREATE INDEX IF NOT EXISTS idx_fav_resource ON favorite (resource_id);

-- 点赞表
CREATE TABLE IF NOT EXISTS resource_like (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    resource_id INTEGER NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_like_user_resource UNIQUE (user_id, resource_id)
);
CREATE INDEX IF NOT EXISTS idx_like_user ON resource_like (user_id);
CREATE INDEX IF NOT EXISTS idx_like_resource ON resource_like (resource_id);

-- 评论表
CREATE TABLE IF NOT EXISTS "comment" (
    id SERIAL PRIMARY KEY,
    resource_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    parent_id INTEGER DEFAULT 0,
    content VARCHAR(1000) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_comment_resource ON "comment" (resource_id);
CREATE INDEX IF NOT EXISTS idx_comment_user ON "comment" (user_id);

-- 下载记录表
CREATE TABLE IF NOT EXISTS download_record (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    resource_id INTEGER NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_dl_user ON download_record (user_id);
CREATE INDEX IF NOT EXISTS idx_dl_resource ON download_record (resource_id);

-- 打赏记录表
CREATE TABLE IF NOT EXISTS reward (
    id SERIAL PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    resource_id INTEGER NOT NULL,
    uploader_id INTEGER NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pay_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_reward_order ON reward (order_no);
CREATE INDEX IF NOT EXISTS idx_reward_user ON reward (user_id);
CREATE INDEX IF NOT EXISTS idx_reward_resource ON reward (resource_id);
